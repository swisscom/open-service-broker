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

package com.swisscom.cloud.sb.broker.backup.job

import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class RestoreJob extends AbstractBackupRestoreJob<Restore> {

    @Override
    protected void markFailure(Restore restore) {
        restore.status = Backup.Status.FAILED
        backupPersistenceService.saveRestore(restore)
    }

    @Override
    protected void markSuccess(Restore restore) {
        restore.status = Backup.Status.SUCCESS
        backupPersistenceService.saveRestore(restore)
    }

    @Override
    protected Backup.Status handleJob(Restore restore) {
        def backupRestoreProvider = findBackupProvider(restore.backup)
        if (Backup.Status.INIT == restore.status) {
            log.info("Handling init status on backup restoration for:${restore}")
            def externalId = backupRestoreProvider.restoreBackup(restore)
            restore.status = Backup.Status.IN_PROGRESS
            restore.externalId = externalId
            backupPersistenceService.saveRestore(restore)
        }
        return backupRestoreProvider.getRestoreStatus(restore)
    }

    @Override
    protected Restore getTargetEntity(String id) {
        return backupPersistenceService.findRestoreByGuid(id)
    }
}
