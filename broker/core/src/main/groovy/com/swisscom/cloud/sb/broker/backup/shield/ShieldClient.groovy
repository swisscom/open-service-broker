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

import com.google.common.base.Preconditions
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

import static com.google.common.base.Preconditions.checkState
import static com.swisscom.cloud.sb.broker.backup.shield.BackupDeregisterInformation.backupDeregisterInformation
import static java.lang.String.format

@Component
@CompileStatic
class ShieldClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShieldClient.class)

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
        LOGGER.info(shieldConfig.toString())
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
        UUID targetUuid = createOrUpdateTarget(shieldTarget, targetName, shieldAgentUrl)
        UUID jobUuid = registerJob(jobName, targetUuid, backupParameter)
        LOGGER.debug("Running registered backup job '{}' with jobname '{}' on shield", jobUuid, jobName)
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
        LOGGER.debug("Registering Job '{}' and Target '{}' to Shield", jobName, targetName)
        UUID targetUuid = createOrUpdateTarget(shieldTarget, targetName, shieldAgentUrl)
        UUID jobUuid = registerJob(jobName, targetUuid, backupParameter, false)
        LOGGER.debug("Running registered system backup job '{}' with jobname '{}' on shield", jobUuid, jobName)
        apiClient.runJob(jobUuid)

        [ServiceDetail.from(ShieldServiceDetailKey.SHIELD_JOB_UUID, jobUuid.toString()),
         ServiceDetail.from(ShieldServiceDetailKey.SHIELD_TARGET_UUID, targetUuid.toString())]
    }

    void unregisterSystemBackup(String jobName, String targetName) {
        Assert.hasText(jobName, "Job name cannot be empty!")
        Assert.hasText(targetName, "Target name cannot be empty!")
        LOGGER.debug("Deregistering Job '{}' and Target '{}' from Shield", jobName, targetName)
        deleteJobsIfExisting(jobName)
        deleteTargetsIfExisting(targetName)
    }

    void deleteTask(String taskUuid) {
        Assert.hasText(taskUuid, "Task UUID cannot be null!")
        apiClient.deleteTaskByUuid(UUID.fromString(taskUuid))
    }

    //FIXME: Do not retry within getJobStatus, as it's not expected
    JobStatus getJobStatus(String taskUuid, Backup backup = null) {
        Assert.notNull(taskUuid, "Task UUID cannot be null!")
        TaskDto task = apiClient.getTaskByUuid(UUID.fromString(taskUuid))
        if (task.status.isRunning()) {
            return JobStatus.RUNNING
        }
        if (task.status.isFailed()) {
            LOGGER.warn("Shield task failed: {}", task)
            if (task.type.isBackup()) {
                if (backup.retryBackupCount < shieldConfig.maxRetryBackup) {
                    LOGGER.info("Retrying backup count: {}", backup.retryBackupCount + 1)
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
        throw new RuntimeException(format("Invalid task status %s for tasks %s", task.status, taskUuid))
    }

    String getJobName(UUID jobUuid) {
        apiClient.getJobByUuid(jobUuid).name
    }

    String restore(String taskUuid) {
        Assert.hasText(taskUuid, "Task UUID cannot be null!")
        TaskDto task = apiClient.getTaskByUuid(UUID.fromString(taskUuid))
        // TODO need to check for archive, status etc. here?
        apiClient.restoreArchive(task.archive_uuid)
    }

    // avoid throwing exceptions in this method so that users can delete failed backups.
    void deleteBackupIfExisting(String taskUuid) {
        try {
            TaskDto task = apiClient.getTaskByUuid(UUID.fromString(taskUuid))
            apiClient.deleteArchive(task.archive_uuid)
        }
        catch (ShieldApiException e) {
            if (e.isNotFound()) {
                // either task or archive is not existing on shield (anymore); probably because it was deleted already
                LOGGER.warn("Could not delete backup because it was not found anymore on Shield; do not fail though", e)
            } else {
                throw e
            }
        }
    }

    /**
     * Delete jobs and targets for a given name
     * @param serviceInstanceId name used in shield search queries
     * @return Map with number of deleted jobs and targets
     */
    public BackupDeregisterInformation deleteJobsAndBackups(String serviceInstanceId) {
        Assert.hasText(serviceInstanceId, "Service Instance GUID cannot be empty!")
        return backupDeregisterInformation().
                deletedJobs(deleteJobsIfExisting(serviceInstanceId)).
                deletedTargets(deleteTargetsIfExisting(serviceInstanceId)).
                build()
    }

    private int deleteJobsIfExisting(String jobName) {
        Collection<JobDto> jobs = apiClient.getJobsByName(jobName)
        if (jobs != null && jobs.size() > 0) {
            jobs.each({job -> apiClient.deleteJob(job.uuid)
            })
            return jobs.size()
        }
        return 0
    }

    private int deleteTargetsIfExisting(String targetName) {
        Collection<TargetDto> targets = apiClient.getTargetsByName(targetName)
        if (targets != null && targets.size() > 0) {
            targets.each {target -> apiClient.deleteTarget(target.uuid)
            }
            return targets.size()
        }
        return 0
    }

    private JobStatus statusOfArchive(TaskDto task) {
        ArchiveDto archive = apiClient.getArchiveByUuid(task.archive_uuid)
        if (archive != null && archive.status.isValid()) {
            return JobStatus.SUCCESSFUL
        } else {
            return JobStatus.FAILED
        }
    }

    private UUID registerJob(String jobName,
                             UUID targetUuid,
                             BackupParameter backupParameter,
                             boolean paused = true) {
        StoreDto store = apiClient.getStoreByName(backupParameter.getStoreName())
        checkState(store != null, format("Store %s that is configured does not exist on shield", backupParameter.getStoreName()))

        RetentionDto retention = apiClient.getRetentionByName(backupParameter.getRetentionName())
        checkState(retention != null, format("Retention %s that is configured does not exist on shield", backupParameter.getRetentionName()))

        UUID scheduleUuid = apiClient.getScheduleByName(backupParameter.getScheduleName()).getUuid()
        checkState(scheduleUuid != null, format("Schedule %s that is configured does not exist on shield", backupParameter.getScheduleName()))

        createOrUpdateJob(jobName, targetUuid, store.getUuid(), retention.getUuid(), scheduleUuid, paused)
    }

    private UUID createOrUpdateTarget(ShieldTarget target, String targetName, String agent) {
        TargetDto targetOnShield = apiClient.getTargetByName(targetName)
        targetOnShield != null && (targetOnShield.getUuid() != null) ? apiClient.
                updateTarget(targetOnShield, target, agent) : apiClient.createTarget(targetName, target, agent)

    }

    private UUID createOrUpdateJob(String jobName,
                                   UUID targetUuid,
                                   UUID storeUuid,
                                   UUID retentionUuid,
                                   UUID scheduleUuid,
                                   boolean paused) {
        JobDto jobOnShield = apiClient.getJobByName(jobName)
        jobOnShield != null && (jobOnShield.getUuid() != null) ?
        apiClient.updateJob(jobOnShield, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused) :
        apiClient.createJob(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
    }
}
