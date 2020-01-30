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

import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
trait BackupRestoreProvider extends BackupOnShield {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupRestoreProvider.class)

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

    /**
     * Backups with externalId set to null are seen as already deleted,
     * and therefore no action is taken
     * @param backup to be deleted
     */
    void deleteBackup(Backup backup) {
        if (backup.externalId != null) {
            shieldClient.deleteBackupIfExisting(backup.externalId)
        }
    }

    Backup.Status getBackupStatus(Backup backup) {
        // for shield, there is no async delete job. there's no need to check for the outcome, it's fine if deleteBackup was successful.
        if (Backup.Operation.DELETE == backup.operation) {
            return Backup.Status.SUCCESS
        }
        JobStatus status = shieldClient.getJobStatus(backup.externalId, backup)
        return convertBackupStatus(status)
    }

    String restoreBackup(Restore restore) {
        shieldClient.restore(restore.backup.externalId)
    }

    Backup.Status getRestoreStatus(Restore restore) {
        JobStatus status = shieldClient.getJobStatus(restore.externalId)
        return convertBackupStatus(status)
    }

    Map<String, Integer> notifyServiceInstanceDeletion(String serviceInstanceGuid) {
        Map<String, Integer> result = shieldClient.deleteJobsAndBackups(serviceInstanceGuid)
        LOGGER.debug("Deleted: {}", result)
        return result
    }

    static Backup.Status convertBackupStatus(JobStatus status) {
        switch (status) {
            case JobStatus.SUCCESSFUL:
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