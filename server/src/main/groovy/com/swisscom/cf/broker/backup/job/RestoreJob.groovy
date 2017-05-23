package com.swisscom.cf.broker.backup.job

import com.swisscom.cf.broker.model.Backup
import com.swisscom.cf.broker.model.Restore
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
