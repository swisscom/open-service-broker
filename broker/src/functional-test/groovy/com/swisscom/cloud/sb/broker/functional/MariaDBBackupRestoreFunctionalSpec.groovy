/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.backup.BackupPersistenceService
import com.swisscom.cloud.sb.broker.repository.BackupRepository
import com.swisscom.cloud.sb.broker.repository.RestoreRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderService
import com.swisscom.cloud.sb.broker.services.mariadb.MariaDBServiceProvider
import com.swisscom.cloud.sb.model.backup.BackupDto
import com.swisscom.cloud.sb.model.backup.BackupStatus
import com.swisscom.cloud.sb.model.backup.RestoreDto
import com.swisscom.cloud.sb.model.backup.RestoreStatus
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.Repository
import org.springframework.http.ResponseEntity
import spock.lang.IgnoreIf
import spock.lang.Shared

@Slf4j
@IgnoreIf({ !Boolean.valueOf(System.properties['com.swisscom.cloud.sb.broker.runMariaDBBackupRestoreFunctionalSpec']) })
class MariaDBBackupRestoreFunctionalSpec extends BaseFunctionalSpec {

    @Shared
    private BackupRepository backupRepository

    @Shared
    private RestoreRepository restoreRepository

    @Autowired
    private BackupPersistenceService backupPersistenceService

    private  BackupRestoreHelper backupRestoreHelper

    @Shared
    private List<String> restoresToCleanup = new ArrayList<>()

    @Shared
    private List<String> backupsToCleanup = new ArrayList<>()

    def setup(){
        serviceLifeCycler.createServiceIfDoesNotExist("mariadb", ServiceProviderService.findInternalName(MariaDBServiceProvider), null, null, null, 5)
        serviceLifeCycler.createParameter('BACKUP_SCHEDULE', 'daily 4am', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_POLICY_NAME', 'month', serviceLifeCycler.plan)
        serviceLifeCycler.createParameter('BACKUP_STORAGE_NAME', 'default', serviceLifeCycler.plan)
        backupRestoreHelper = new BackupRestoreHelper(appBaseUrl, cfExtUser.username, cfExtUser.password)
    }

    @Autowired
    def injectObjectConfig(BackupRepository backupRepository, RestoreRepository restoreRepository) {
        this.backupRepository = backupRepository
        this.restoreRepository = restoreRepository
    }

    def cleanupBackupsRestores() {
        cleanupRestores()
        cleanupBackups()
    }

    def cleanupSpec(){
        this.cleanupBackupsRestores()
        serviceLifeCycler.cleanup()
    }

    def "provision and bind MariaDB service instance"(){
        given:
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert()
        def credentialJson = serviceLifeCycler.getCredentials()

        when: 'data is created in mariaDB'
        def tableResultAfterCreation, funcResultAfterCreation, procResultAfterCreation, eventsAfterCreation
        Sql.withInstance(credentialJson.jdbcUrl, {
            Sql sql ->
                sql.execute("CREATE TABLE new_table1 (`idnew_table` INT NULL);")
                sql.execute("INSERT INTO new_table1 VALUES(1);")
                tableResultAfterCreation = sql.firstRow("SELECT idnew_table FROM new_table1").idnew_table

                sql.execute("CREATE FUNCTION func1 () RETURNS INT RETURN 1;")
                funcResultAfterCreation = sql.firstRow("SELECT func1() as one").one

                sql.execute("CREATE PROCEDURE proc1 (OUT id INT) BEGIN select idnew_table INTO id from new_table1; END")
                sql.call("{ call proc1(?) }", [Sql.INTEGER]) { res ->
                    procResultAfterCreation = res
                }

                sql.execute("CREATE EVENT event1 ON SCHEDULE EVERY 1 HOUR DO BEGIN SELECT 1 FROM new_table1; END")
                eventsAfterCreation = sql.rows("SHOW EVENTS")
        })
        tableResultAfterCreation == 1
        funcResultAfterCreation == 1
        procResultAfterCreation == 1
        eventsAfterCreation.size() == 1
        String instance = serviceLifeCycler.getServiceInstanceId()
        and: 'backup is created'
        ResponseEntity<BackupDto> createBackupRestResponse = backupRestoreHelper.createBackup(instance)
        def createBackupDto = createBackupRestResponse.getBody()
        waitABit()
        ResponseEntity<BackupDto> getBackupRestResponse = backupRestoreHelper.getBackup(instance, createBackupDto.id)
        def getBackupDto = getBackupRestResponse.getBody()
        getBackupDto.getStatus() == BackupStatus.CREATE_SUCCEEDED

        and: 'data is deleted'
        Sql.withInstance(credentialJson.jdbcUrl, {
            Sql sql ->
                sql.execute("DROP TABLE new_table1;")
                sql.execute("DROP FUNCTION func1;")
                sql.execute("DROP PROCEDURE proc1;")
                sql.execute("DROP EVENT event1;")
        })

        and: 'backup is restored'
        ResponseEntity<RestoreDto> createRestoreRestResponse = backupRestoreHelper.restoreBackup(instance, createBackupDto.id)
        def createRestoreDto = createRestoreRestResponse.getBody()

        createRestoreDto.getStatus() == RestoreStatus.IN_PROGRESS
        waitABit()
        ResponseEntity<RestoreDto> getRestoreRestResponse = backupRestoreHelper.getRestore(instance, createBackupDto.id, createRestoreDto.id)
        def getRestoreDto = getRestoreRestResponse.getBody()
        restoresToCleanup.add(createRestoreDto.id)
        getRestoreDto.status == RestoreStatus.SUCCEEDED

        then: 'data is there again'
        def tableResultAfterRestore, funcResultAfterRestore, procResultAfterRestore, eventsAfterRestore
        Sql.withInstance(credentialJson.jdbcUrl, {
            Sql sql ->
                tableResultAfterRestore = sql.firstRow("SELECT idnew_table FROM new_table1").idnew_table
                funcResultAfterCreation = sql.firstRow("SELECT func1() as one").one

                sql.call("{ call proc1(?) }", [Sql.INTEGER]) { res ->
                    procResultAfterCreation = res
                }

                eventsAfterRestore = sql.rows("SHOW EVENTS")
        })
        tableResultAfterRestore == 1
        tableResultAfterRestore == 1
        tableResultAfterRestore == 1
        eventsAfterRestore.size() == 1

        and: 'backup is deleted'
        ResponseEntity deleteBackupRestResponse = backupRestoreHelper.deleteBackup(instance, createBackupDto.id)
        // Quartz needs time to finish, otherwise dirty fragments remain after testing
        waitABit()
        backupsToCleanup.add(createBackupDto.id)
        deleteBackupRestResponse.getStatusCodeValue() == 202
    }

    def "unbind and deprovision MariaDB service instance" (){
        expect:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()
    }

    private void waitABit() {
        serviceLifeCycler.pauseExecution(40)
    }

    private void cleanupRestores() {
        cleanupObjectsInDB(restoreRepository, restoresToCleanup)
        restoresToCleanup.clear()
    }


    private void cleanupBackups() {
        cleanupObjectsInDB(backupRepository, backupsToCleanup)
        backupsToCleanup.clear()
    }

    private void cleanupObjectsInDB(Repository repo, List<?> objectsToCleanup) {
        for (String guid : objectsToCleanup) {
            def object = repo.findByGuid(guid)
            repo.delete(object)
        }
    }
}
