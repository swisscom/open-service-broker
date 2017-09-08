package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.backup.BackupRestoreProvider
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobStatus
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import org.springframework.beans.factory.annotation.Autowired

trait ShieldBackupRestoreProvider implements BackupRestoreProvider {
    @Autowired
    ShieldClient shieldClient

    abstract ShieldTarget targetForBackup(Backup backup)

    @Override
    String createBackup(Backup backup) {
        shieldClient.registerAndRunJob(backup.serviceInstanceGuid, targetForBackup(backup))
    }

    @Override
    void deleteBackup(Backup backup) {
        shieldClient.deleteBackup(backup.externalId)
    }

    @Override
    Backup.Status getBackupStatus(Backup backup) {
        try {
            JobStatus status = shieldClient.getJobStatus(backup.externalId)
            return convertBackupStatus(status)
        } catch (ShieldResourceNotFoundException e) {
            if (Backup.Operation.DELETE == backup.operation) {
                return Backup.Status.SUCCESS
            } else {
                throw e
            }
        }
    }

    @Override
    String restoreBackup(Restore restore) {
        shieldClient.restore(restore.backup.externalId)
    }

    @Override
    Backup.Status getRestoreStatus(Restore restore) {
        JobStatus status = shieldClient.getJobStatus(restore.externalId)
        return convertBackupStatus(status)
    }

    @Override
    void notifyServiceInstanceDeletion(ServiceInstance serviceInstance) {
        shieldClient.deleteJobsAndBackups(serviceInstance.guid)
    }

    public static Backup.Status convertBackupStatus(JobStatus status) {
        switch (status) {
            case JobStatus.FINISHED:
                return Backup.Status.SUCCESS
            case JobStatus.FAILED:
                return Backup.Status.FAILED
            case JobStatus.RUNNING:
                return Backup.Status.IN_PROGRESS
            default:
                throw new RuntimeException("Unknown enum type: ${status.toString()}")
        }
    }
}
