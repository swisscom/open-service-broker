package com.swisscom.cloud.sb.broker.backup.job

import com.swisscom.cloud.sb.broker.model.Backup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class BackupDeletionJob extends AbstractBackupJob {

    @Override
    protected Backup.Status handleJob(Backup backup) {
        def backupRestoreProvider = findBackupProvider(backup)
        if (Backup.Status.INIT == backup.status) {
            log.info("Handling init status on backup deletion for:${backup}")
            backupRestoreProvider.deleteBackup(backup)
            backup.status = Backup.Status.IN_PROGRESS
            backupPersistenceService.saveBackup(backup)
        }
        return backupRestoreProvider.getBackupStatus(backup)
    }
}
