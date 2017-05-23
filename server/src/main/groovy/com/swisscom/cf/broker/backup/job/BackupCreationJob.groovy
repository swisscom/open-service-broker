package com.swisscom.cf.broker.backup.job

import com.swisscom.cf.broker.model.Backup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class BackupCreationJob extends AbstractBackupJob {

    @Override
    protected Backup.Status handleJob(Backup backup) {
        def backupRestoreProvider = findBackupProvider(backup)
        if (Backup.Status.INIT == backup.status) {
            log.info("Handling init status on backup creation for:${backup}")
            String externalId = backupRestoreProvider.createBackup(backup)
            backup.status = Backup.Status.IN_PROGRESS
            backup.externalId = externalId
            backupPersistenceService.saveBackup(backup)
        }
        return backupRestoreProvider.getBackupStatus(backup)
    }
}
