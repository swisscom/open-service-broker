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

package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.backup.config.BackupConfig
import com.swisscom.cloud.sb.broker.backup.job.BackupCreationJob
import com.swisscom.cloud.sb.broker.backup.job.BackupDeletionJob
import com.swisscom.cloud.sb.broker.backup.job.RestoreJob
import com.swisscom.cloud.sb.broker.backup.job.config.BackupCreationJobConfig
import com.swisscom.cloud.sb.broker.backup.job.config.BackupDeletionJobConfig
import com.swisscom.cloud.sb.broker.backup.job.config.RestoreJobConfig
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.StringGenerator
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
@Slf4j
class BackupService {
    @Autowired
    protected JobManager jobManager

    @Autowired
    protected BackupPersistenceService backupPersistenceService

    @Autowired
    protected BackupRestoreProviderLookup backupRestoreProviderLookup

    @Autowired
    protected BackupConfig backupConfig

    Collection<Backup> listBackups(ServiceInstance serviceInstance) {
        checkBackupEnabled(serviceInstance)
        return backupPersistenceService.findAllBackupsForServiceInstance(serviceInstance)
    }

    Backup getBackup(ServiceInstance si, String backupId) {
        checkBackupEnabled(si)
        return getAndCheckBackup(backupId)
    }

    Backup requestBackupCreation(ServiceInstance serviceInstance) {
        checkBackupEnabled(serviceInstance)
        checkBackupLimit(serviceInstance)
        checkConcurrentBackupsOrRestores(serviceInstance)
        checkServiceInstanceCreationCompletion(serviceInstance)

        def backup = backupPersistenceService.createBackup(serviceInstance, StringGenerator.randomUuid())
        queueBackupCreation(backup)
        return backup
    }


    private void checkBackupLimit(ServiceInstance serviceInstance) {
        if (getNumberOfBackups(serviceInstance) >= serviceInstance.plan.maxBackups) {
            ErrorCode.BACKUP_LIMIT_EXCEEDED.throwNew()
        }
    }

    private int getNumberOfBackups(ServiceInstance serviceInstance) {
        return backupPersistenceService.findAllBackupsForServiceInstance(serviceInstance).findAll({ Backup b ->
            b.operation == Backup.Operation.CREATE && b.status != Backup.Status.FAILED
        }).size()
    }

    private void checkConcurrentBackupsOrRestores(ServiceInstance serviceInstance) {
        def backups = backupPersistenceService.findAllBackupsForServiceInstance(serviceInstance).findAll { Backup b -> b.operation == Backup.Operation.CREATE }
        if (anyOngoingBackups(backups) || anyOngoingRestores(backups)) {
            ErrorCode.BACKUP_CONCURRENT_OPERATION.throwNew()
        }
    }

    private checkServiceInstanceCreationCompletion(ServiceInstance serviceInstance) {
        if (!serviceInstance.completed) {
            ErrorCode.SERVICE_INSTANCE_NOT_READY.throwNew()
        }
    }

    private static boolean anyOngoingRestores(Collection<Backup> backups) {
        def result = false
        backups.each { Backup b ->
            b.restores.each { Restore r ->
                result |= (!r.status.isFinalState)
            }
        }
        return result
    }

    private static boolean anyOngoingBackups(Collection<Backup> backups) {
        return backups.any { !it.status.isFinalState }
    }

    private void queueBackupCreation(Backup backup) {
        jobManager.queue(new BackupCreationJobConfig(BackupCreationJob.class, backup.guid, backupConfig.retryIntervalInSeconds, backupConfig.maxRetryDurationInMinutes, backup))
    }

    void requestBackupDeletion(ServiceInstance serviceInstance, String backupId) {
        checkBackupEnabled(serviceInstance)

        def backup = getAndCheckBackup(backupId)
        checkOngoingRestore(backup)

        checkIfAlreadyMarkedForDeletion(backup)
        checkServiceInstanceCreationCompletion(serviceInstance)

        backup = backupPersistenceService.updateBackupOperation(backup, Backup.Operation.DELETE)

        queueBackupDeletion(backup)
    }

    private void checkOngoingRestore(Backup backup) {
        if (anyOngoingRestores([backup])) {
            ErrorCode.BACKUP_CONCURRENT_OPERATION.throwNew()
        }
    }

    private void checkIfAlreadyMarkedForDeletion(Backup backup) {
        if (Backup.Operation.DELETE == backup.operation) {
            if (Backup.Status == Backup.Status.SUCCESS) {
                log.info("Backup with:${backup.guid} is already deleted (awaiting cleanup job)")
                ErrorCode.BACKUP_NOT_FOUND.throwNew()
            } else if (Backup.Status == Backup.Status.IN_PROGRESS) {
                log.info("Backup with:${backup.guid} is already scheduled for deletion")
                ErrorCode.BACKUP_CONCURRENT_OPERATION.throwNew("Delete already in progress")
            }
        }
    }

    private Backup getAndCheckBackup(String backupId) {
        Backup backup = backupPersistenceService.findBackupByGuid(backupId)
        if (!backup) {
            ErrorCode.BACKUP_NOT_FOUND.throwNew()
        }
        return backup
    }

    private void queueBackupDeletion(Backup backup) {
        jobManager.queue(new BackupDeletionJobConfig(BackupDeletionJob.class, backup.guid, backupConfig.retryIntervalInSeconds, backupConfig.maxRetryDurationInMinutes, backup))
    }

    Restore requestBackupRestoration(ServiceInstance serviceInstance, String backupId) {
        checkBackupEnabled(serviceInstance)
        checkConcurrentBackupsOrRestores(serviceInstance)
        checkServiceInstanceCreationCompletion(serviceInstance)

        def backup = getAndCheckBackup(backupId)
        checkIfBackupReadyForRestoration(backup)

        def restore = backupPersistenceService.createRestore(backup, StringGenerator.randomUuid())

        queueBackupRestoration(restore)
        return restore
    }

    def checkIfBackupReadyForRestoration(Backup backup) {
        if (backup.operation != Backup.Operation.CREATE || backup.status != Backup.Status.SUCCESS) {
            ErrorCode.RESTORE_NOT_ALLOWED.throwNew()
        }
    }

    private void queueBackupRestoration(Restore restore) {
        jobManager.queue(new RestoreJobConfig(RestoreJob.class, restore.guid, backupConfig.retryIntervalInSeconds, backupConfig.maxRetryDurationInMinutes, restore))
    }

    Restore getRestore(ServiceInstance serviceInstance, String backupId, String restoreId) {
        checkBackupEnabled(serviceInstance)
        return getAndCheckRestore(restoreId)
    }

    private Restore getAndCheckRestore(String restoreId) {
        def restore = backupPersistenceService.findRestoreByGuid(restoreId)
        if (!restore) {
            ErrorCode.RESTORE_NOT_FOUND.throwNew()
        }
        return restore
    }

    private void checkBackupEnabled(ServiceInstance serviceInstance) {
        if (!isBackupEnabled(serviceInstance)) {
            ErrorCode.BACKUP_NOT_ENABLED.throwNew()
        }
    }

    void notifyServiceInstanceDeletion(ServiceInstance serviceInstance) {
        if (backupRestoreProviderLookup.isBackupProvider(serviceInstance.plan)) {
            backupRestoreProviderLookup.findBackupProvider(serviceInstance.plan).notifyServiceInstanceDeletion(serviceInstance)
        }
    }

    boolean isBackupEnabled(ServiceInstance serviceInstance) {
        serviceInstance.plan.maxBackups > 0 && backupRestoreProviderLookup.isBackupProvider(serviceInstance.plan)
    }
}
