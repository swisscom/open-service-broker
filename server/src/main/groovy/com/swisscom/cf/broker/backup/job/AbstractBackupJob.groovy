package com.swisscom.cf.broker.backup.job

import com.swisscom.cf.broker.model.Backup
import groovy.transform.CompileStatic

@CompileStatic
abstract class AbstractBackupJob extends AbstractBackupRestoreJob<Backup> {

    @Override
    protected void markFailure(Backup backup) {
        backup.status = Backup.Status.FAILED
        backupPersistenceService.saveBackup(backup)
    }

    @Override
    protected void markSuccess(Backup backup) {
        backup.status = Backup.Status.SUCCESS
        backupPersistenceService.saveBackup(backup)
    }

    @Override
    protected Backup getTargetEntity(String id) {
        return backupPersistenceService.findBackupByGuid(id)
    }
}
