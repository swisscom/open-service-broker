package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.backup.shield.dto.JobStatus
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
trait BackupRestoreProvider extends BackupOnShield {
    def userBackupJobName(String jobPrefix, String serviceInstanceId) {
        backupJobName(jobPrefix, serviceInstanceId)
    }

    def userBackupTargetName(String targetPrefix, String serviceInstanceId) {
        backupTargetName(targetPrefix, serviceInstanceId)
    }

    String createBackup(Backup backup) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(backup.serviceInstanceGuid)
        def shieldServiceConfig = getBackupParameter(serviceInstance)
        def shieldTarget = buildShieldTarget(serviceInstance)
        String jobName = userBackupJobName(shieldConfig.jobPrefix, serviceInstance.guid)
        String targetName = userBackupTargetName(shieldConfig.targetPrefix, serviceInstance.guid)
        shieldClient.registerAndRunJob(jobName, targetName, shieldTarget, shieldServiceConfig, shieldAgentUrl(serviceInstance))
    }

    void deleteBackup(Backup backup) {
        shieldClient.deleteBackup(backup.externalId)
    }

    Backup.Status getBackupStatus(Backup backup) {
        // for shield, there is no async delete job. there's no need to check for the outcome, it's fine if deleteBackup was successful.
        if (Backup.Operation.DELETE == backup.operation) {
            return Backup.Status.SUCCESS
        }
        TaskDto task = getTask(backup.externalId)
        JobStatus status = getJobStatus(task)
        return convertBackupStatus(status)
    }

    String restoreBackup(Restore restore) {
        shieldClient.restore(restore.backup.externalId)
    }

    Backup.Status getRestoreStatus(Restore restore) {
        TaskDto task = getTask(restore.externalId)
        JobStatus status = getJobStatus(task)
        return convertBackupStatus(status)
    }

    @Override
    JobStatus getJobStatus(TaskDto task) {
        if (task.statusParsed.isRunning()) {
            return JobStatus.RUNNING
        }
        if (task.statusParsed.isFailed()) {
            return JobStatus.FAILED
        }
        if (task.statusParsed.isDone()) {
            if (task.typeParsed.isBackup()) {
                // if backup tasks are done, they should have an associated archive now
                return shieldClient.statusOfArchive(task)
            } else {
                // if it's a restore task, it's finished when done.
                return JobStatus.FINISHED
            }
        }
        throw new RuntimeException("Invalid task status ${task.status} for task ${task.job_uuid}")
    }

    void notifyServiceInstanceDeletion(ServiceInstance serviceInstance) {
        shieldClient.deleteJobsAndBackups(serviceInstance.guid)
    }

    static Backup.Status convertBackupStatus(JobStatus status) {
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