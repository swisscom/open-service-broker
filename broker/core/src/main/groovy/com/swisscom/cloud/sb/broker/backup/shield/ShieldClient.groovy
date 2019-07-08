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

package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.async.job.JobStatus
import com.swisscom.cloud.sb.broker.backup.BackupPersistenceService
import com.swisscom.cloud.sb.broker.backup.shield.dto.*
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.util.Assert

@Component
@CompileStatic
class ShieldClient {
    private static final Logger LOG = LoggerFactory.getLogger(ShieldClient.class)

    private final ShieldConfig shieldConfig
    private final ShieldRestClient apiClient
    private final RestTemplateBuilder restTemplateBuilder
    private final BackupPersistenceService backupPersistenceService

    @Autowired
    ShieldClient(ShieldConfig shieldConfig,
                 BackupPersistenceService backupPersistenceService) {
        Assert.notNull(shieldConfig, "Shield config cannot be null!")
        Assert.notNull(backupPersistenceService, "Backup persistence service cannot be null!")
        this.shieldConfig = shieldConfig
        this.apiClient = ShieldRestClientV1.of(shieldConfig)
        this.restTemplateBuilder = new RestTemplateBuilder()
        this.backupPersistenceService = backupPersistenceService
        LOG.info(shieldConfig.toString())
    }

    String registerAndRunJob(String jobName,
                             String targetName,
                             ShieldTarget shieldTarget,
                             BackupParameter backupParameter,
                             String shieldAgentUrl) {
        Assert.hasText(jobName, "Job name cannot be empty!")
        Assert.hasText(targetName, "Target name cannot be empty!")
        Assert.notNull(shieldTarget, "ShieldTarget cannot be null!")
        Assert.notNull(backupParameter, "BackupParameter cannot be null!")
        Assert.hasText(shieldAgentUrl, "Shield agent URL cannot be empty!")
        String targetUuid = createOrUpdateTarget(shieldTarget, targetName, shieldAgentUrl)
        String jobUuid = registerJob(jobName, targetUuid, backupParameter)
        LOG.debug("Running registered backup job '{}' with jobname '{}' on shield", jobUuid, jobName)
        apiClient.runJob(jobUuid)
    }

    Collection<ServiceDetail> registerAndRunSystemBackup(String jobName,
                                                         String targetName,
                                                         ShieldTarget shieldTarget,
                                                         BackupParameter backupParameter,
                                                         String shieldAgentUrl) {
        Assert.hasText(jobName, "Job name cannot be empty!")
        Assert.hasText(targetName, "Target name cannot be empty!")
        Assert.notNull(shieldTarget, "ShieldTarget cannot be null!")
        Assert.notNull(backupParameter, "BackupParameter cannot be null!")
        Assert.hasText(shieldAgentUrl, "Shield agent URL cannot be empty!")
        LOG.debug("Registering Job '{}' and Target '{}' to Shield", jobName, targetName)
        String targetUuid = createOrUpdateTarget(shieldTarget, targetName, shieldAgentUrl)
        String jobUuid = registerJob(jobName, targetUuid, backupParameter, false)
        LOG.debug("Running registered system backup job '{}' with jobname '{}' on shield", jobUuid, jobName)
        apiClient.runJob(jobUuid)

        [ServiceDetail.from(ShieldServiceDetailKey.SHIELD_JOB_UUID, jobUuid),
         ServiceDetail.from(ShieldServiceDetailKey.SHIELD_TARGET_UUID, targetUuid)]
    }

    void unregisterSystemBackup(String jobName, String targetName) {
        Assert.hasText(jobName, "Job name cannot be empty!")
        Assert.hasText(targetName, "Target name cannot be empty!")
        LOG.debug("Deregistering Job '{}' and Target '{}' from Shield", jobName, targetName)
        deleteJobIfExisting(jobName)
        deleteTargetIfExisting(targetName)
    }

    void deleteTask(String taskUuid) {
        Assert.hasText(taskUuid, "Task UUID cannot be empty!")
        apiClient.deleteTaskByUuid(taskUuid)
    }

    //FIXME: Do not retry within getJobStatus, as it's not expected
    JobStatus getJobStatus(String taskUuid, Backup backup = null) {
        Assert.hasText(taskUuid, "Task UUID cannot be empty!")
        TaskDto task = apiClient.getTaskByUuid(taskUuid)
        if (task.status.isRunning()) {
            return JobStatus.RUNNING
        }
        if (task.status.isFailed()) {
            LOG.warn("Shield task failed: ${task}")
            if (task.type.isBackup()) {
                if (backup.retryBackupCount < shieldConfig.maxRetryBackup) {
                    LOG.info("Retrying backup count: ${backup.retryBackupCount + 1}")
                    backup.retryBackupCount++
                    backup.externalId = apiClient.runJob(task.job_uuid)
                    backupPersistenceService.saveBackup(backup)
                    return JobStatus.RUNNING
                } else {
                    return JobStatus.FAILED
                }
            } else {
                return JobStatus.FAILED
            }
        }
        if (task.status.isDone()) {
            if (task.type.isBackup()) {
                // if backup tasks are done, they should have an associated archive now
                return statusOfArchive(task)
            } else {
                // if it's a restore task, it's finished when done.
                return JobStatus.SUCCESSFUL
            }
        }
        throw new RuntimeException("Invalid task status ${task.status} for task ${taskUuid}")
    }

    String getJobName(String jobUuid) {
        Assert.hasText(jobUuid, "Job UUID cannot be empty!")
        apiClient.getJobByUuid(jobUuid).name
    }

    String restore(String taskUuid) {
        Assert.hasText(taskUuid, "Task UUID cannot be empty!")
        TaskDto task = apiClient.getTaskByUuid(taskUuid)
        // TODO need to check for archive, status etc. here?
        apiClient.restoreArchive(task.archive_uuid)
    }

    // avoid throwing exceptions in this method so that users can delete failed backups.
    void deleteBackupIfExisting(String taskUuid) {
        Assert.hasText(taskUuid, "Task UUID cannot be empty!")
        try {
            TaskDto task = apiClient.getTaskByUuid(taskUuid)
            apiClient.deleteArchive(task.archive_uuid)
        }
        catch (ShieldApiException e) {
            if (e.isNotFound()) {
                // either task or archive is not existing on shield (anymore); probably because it was deleted already
                LOG.warn("Could not delete backup because it was not found anymore on Shield; do not fail though", e)
            } else {
                throw e
            }
        }
    }

    void deleteJobsAndBackups(String serviceInstanceId) {
        Assert.hasText(serviceInstanceId, "Service Instance GUID cannot be empty!")
        deleteJobIfExisting(serviceInstanceId)
        deleteTargetIfExisting(serviceInstanceId)
    }

    private void deleteJobIfExisting(String jobName) {
        JobDto job = apiClient.getJobByName(jobName)
        if (job != null) {
            apiClient.deleteJob(job.uuid)
        }
    }

    private void deleteTargetIfExisting(String targetName) {
        TargetDto target = apiClient.getTargetByName(targetName)
        if (target != null) {
            apiClient.deleteTarget(target.uuid)
        }
    }

    private JobStatus statusOfArchive(TaskDto task) {
        ArchiveDto archive = apiClient.getArchiveByUuid(task.archive_uuid)
        if (archive != null && archive.status.isValid()) {
            return JobStatus.SUCCESSFUL
        } else {
            return JobStatus.FAILED
        }
    }

    private String registerJob(String jobName,
                               String targetUuid,
                               BackupParameter backupParameter,
                               boolean paused = true) {
        StoreDto store = apiClient.getStoreByName(backupParameter.getStoreName())
        if (store == null) {
            throw new IllegalStateException("Store ${backupParameter.getStoreName()} that is configured does not exist on shield")
        }
        RetentionDto retention = apiClient.getRetentionByName(backupParameter.getRetentionName())
        if (retention == null) {
            throw new IllegalStateException("Retention ${backupParameter.getRetentionName()} that is configured does not exist on shield")
        }

        // Either use BACKUP_SCHEDULE parameter or get the schedule UUID from shield v1 BACKUP_SCHEDULE_NAME parameter from service definition
        String schedule = backupParameter.getScheduleName().isEmpty() ? backupParameter.getSchedule() :
                          apiClient.getScheduleByName(backupParameter.getScheduleName()).getUuid()
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalStateException("Schedule ${backupParameter.getScheduleName()} that is configured does not exist on shield")
        }

        createOrUpdateJob(jobName, targetUuid, store.getUuid(), retention.getUuid(), schedule, paused)
    }

    private String createOrUpdateTarget(ShieldTarget target, String targetName, String agent) {
        TargetDto targetOnShield = apiClient.getTargetByName(targetName)
        targetOnShield != null && (targetOnShield.getUuid() != null && targetOnShield.getUuid().length() > 0) ? apiClient.
                updateTarget(targetOnShield, target, agent) : apiClient.createTarget(targetName, target, agent)

    }

    private String createOrUpdateJob(String jobName,
                                     String targetUuid,
                                     String storeUuid,
                                     String retentionUuid,
                                     String scheduleUuid,
                                     boolean paused) {
        JobDto jobOnShield = apiClient.getJobByName(jobName)
        jobOnShield != null && (jobOnShield.getUuid() != null && jobOnShield.getUuid().length() > 0) ?
        apiClient.updateJob(jobOnShield, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused) :
        apiClient.createJob(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
    }
}
