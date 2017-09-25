package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.backup.shield.dto.*
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class ShieldClient {
    protected ShieldConfig shieldConfig
    protected ShieldRestClientFactory shieldRestClientFactory
    protected RestTemplateBuilder restTemplateBuilder

    @Autowired
    ShieldClient(ShieldConfig shieldConfig, ShieldRestClientFactory shieldRestClientFactory, RestTemplateBuilder restTemplateBuilder) {
        this.shieldConfig = shieldConfig
        this.shieldRestClientFactory = shieldRestClientFactory
        this.restTemplateBuilder = restTemplateBuilder
    }

    String registerAndRunJob(String jobName, String targetName, ShieldTarget shieldTarget, BackupParameter shieldServiceConfig, String shieldAgentUrl) {
        String targetUuid = createOrUpdateTarget(shieldTarget, targetName, shieldAgentUrl)
        String jobUuid = registerJob(jobName, targetUuid, shieldServiceConfig)

        buildClient().runJob(jobUuid)
    }

    Collection<ServiceDetail> registerAndRunSystemBackup(String jobName, String targetName, ShieldTarget shieldTarget, BackupParameter shieldServiceConfig, String shieldAgentUrl) {
        String targetUuid = createOrUpdateTarget(shieldTarget, targetName, shieldAgentUrl)
        String jobUuid = registerJob(jobName, targetUuid, shieldServiceConfig, false)

        buildClient().runJob(jobUuid)

        [ServiceDetail.from(ShieldServiceDetailKey.SHIELD_JOB_UUID, jobUuid),
         ServiceDetail.from(ShieldServiceDetailKey.SHIELD_TARGET_UUID, targetUuid)]
    }

    def unregisterSystemBackup(String jobName, String targetName) {
        deleteJobIfExisting(jobName)
        deleteTargetIfExisting(targetName)
    }

    JobStatus getJobStatus(String taskUuid) {
        TaskDto task = buildClient().getTaskByUuid(taskUuid)
        if (task.statusParsed.isRunning()) {
            return JobStatus.RUNNING
        }
        if (task.statusParsed.isFailed()) {
            log.warn("Shield task failed: ${task}")
            return JobStatus.FAILED
        }
        if (task.statusParsed.isDone()) {
            if (task.typeParsed.isBackup()) {
                // if backup tasks are done, they should have an associated archive now
                return statusOfArchive(task)
            } else {
                // if it's a restore task, it's finished when done.
                return JobStatus.FINISHED
            }
        }
        throw new RuntimeException("Invalid task status ${task.status} for task ${taskUuid}")
    }

    String getJobName(String jobUuid) {
        buildClient().getJobByUuid(jobUuid).name
    }

    String restore(String taskUuid) {
        TaskDto task = buildClient().getTaskByUuid(taskUuid)
        // TODO need to check for archive, status etc. here?
        buildClient().restoreArchive(task.archive_uuid)
    }

    // avoid throwing exceptions in this method so that users can delete failed backups.
    void deleteBackup(String taskUuid) {
        try {
            TaskDto task = buildClient().getTaskByUuid(taskUuid)
            buildClient().deleteArchive(task.archive_uuid)
        }
        catch (ShieldResourceNotFoundException e) {
            // either task or archive is not existing on shield (anymore); probably because it was deleted already
            log.warn("Could not delete backup because it was not found anymore on Shield; do not fail though", e)
        }
    }

    void deleteJobsAndBackups(String serviceInstanceId) {
        deleteJobIfExisting(serviceInstanceId)
        deleteTargetIfExisting(serviceInstanceId)
    }

    private deleteJobIfExisting(String jobName) {
        JobDto job = buildClient().getJobByName(jobName)
        if (job != null) {
            buildClient().deleteJob(job.uuid)
        }
    }

    private deleteTargetIfExisting(String targetName) {
        TargetDto target = buildClient().getTargetByName(targetName)
        if (target != null) {
            buildClient().deleteTarget(target.uuid)
        }
    }

    private JobStatus statusOfArchive(TaskDto task) {
        ArchiveDto archive = buildClient().getArchiveByUuid(task.archive_uuid)
        if (archive != null && archive.statusParsed.isValid()) {
            return JobStatus.FINISHED
        } else {
            return JobStatus.FAILED
        }
    }

    private String registerJob(String jobName, String targetUuid, BackupParameter shieldServiceConfig, boolean paused = true) {
        StoreDto store = buildClient().getStoreByName(shieldServiceConfig.storeName)
        if (store == null) {
            throw new RuntimeException("Store ${shieldServiceConfig.storeName} that is configured does not exist on shield")
        }
        RetentionDto retention = buildClient().getRetentionByName(shieldServiceConfig.retentionName)
        if (retention == null) {
            throw new RuntimeException("Retention ${shieldServiceConfig.retentionName} that is configured does not exist on shield")
        }
        ScheduleDto schedule = buildClient().getScheduleByName(shieldServiceConfig.scheduleName)
        if (schedule == null) {
            throw new RuntimeException("Schedule ${shieldServiceConfig.scheduleName} that is configured does not exist on shield")
        }

        createOrUpdateJob(jobName, targetUuid, store.uuid, retention.uuid, schedule.uuid, paused)
    }

    private String createOrUpdateTarget(ShieldTarget target, String targetName, String agent) {
        TargetDto targetOnShield = buildClient().getTargetByName(targetName)
        targetOnShield == null ? buildClient().createTarget(targetName, target, agent) : buildClient().updateTarget(targetOnShield, target, agent)
    }

    private String createOrUpdateJob(String jobName,
                                     String targetUuid,
                                     String storeUuid,
                                     String retentionUuid,
                                     String scheduleUuid,
                                     boolean paused) {
        JobDto jobOnShield = buildClient().getJobByName(jobName)
        jobOnShield == null ?
                buildClient().createJob(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused) :
                buildClient().updateJob(jobOnShield, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
    }

    private ShieldRestClient buildClient() {
        shieldRestClientFactory.build(restTemplateBuilder.withSSLValidationDisabled().build(), shieldConfig.baseUrl, shieldConfig.apiKey)
    }
}
