package com.swisscom.cf.broker.backup

import com.swisscom.cf.broker.model.Backup
import com.swisscom.cf.broker.model.Restore
import com.swisscom.cf.broker.model.ServiceInstance
import groovy.transform.CompileStatic

@CompileStatic
interface BackupRestoreProvider {
    String createBackup(Backup backup)

    void deleteBackup(Backup backup)

    Backup.Status getBackupStatus(Backup backup)

    String restoreBackup(Restore restore)

    Backup.Status getRestoreStatus(Restore restore)

    void notifyServiceInstanceDeletion(ServiceInstance serviceInstance)
}