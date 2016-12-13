package com.swisscom.cf.broker.backup.job

import com.swisscom.cf.broker.model.Backup
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

@CompileStatic
@Log4j
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
