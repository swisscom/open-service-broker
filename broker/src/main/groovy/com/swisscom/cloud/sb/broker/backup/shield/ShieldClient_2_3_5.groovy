package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.backup.shield.dto.*
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Slf4j
class ShieldClient_2_3_5 {
    protected ShieldConfig shieldConfig
    protected ShieldRestClientFactory shieldRestClientFactory

    @Autowired
    ShieldClient_2_3_5(ShieldConfig shieldConfig, ShieldRestClientFactory shieldRestClientFactory) {
        this.shieldConfig = shieldConfig
        this.shieldRestClientFactory = shieldRestClientFactory
    }

    String registerAndRunJob(String serviceInstanceId, ShieldTarget target) {
        String jobId = registerJob(serviceInstanceId, target)
        return buildClient().runJob(jobId)
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

    String restore(String taskUuid) {
        TaskDto task = buildClient().getTaskByUuid(taskUuid)
        // TODO need to check for archive, status etc. here?
        buildClient().restoreArchive(task.archive_uuid)
    }

    void deleteBackup(String taskUuid) {
        TaskDto task = buildClient().getTaskByUuid(taskUuid)
        // TODO need to check for archive, status etc. here?
        buildClient().deleteArchive(task.archive_uuid)
    }

    void deleteJobsAndBackups(String serviceInstanceId) {
        deleteJobIfExisting(serviceInstanceId)
        deleteTargetIfExisting(serviceInstanceId)
    }

    private deleteJobIfExisting(String serviceInstanceId) {
        String jobName = targetName(serviceInstanceId)
        JobDto job = buildClient().getJobByName(jobName)
        if (job != null) {
            buildClient().deleteJob(job.uuid)
        }
    }

    private deleteTargetIfExisting(String serviceInstanceId) {
        String targetName = targetName(serviceInstanceId)
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

    private String registerJob(String serviceInstanceId, ShieldTarget target) {
        String targetUuid = createOrUpdateTarget(serviceInstanceId, target)
        StoreDto store = buildClient().getStoreByName(shieldConfig.storeName)
        if (store == null) {
            throw new RuntimeException("Store ${shieldConfig.storeName} that is configured does not exist on shield")
        }
        RetentionDto retention = buildClient().getRetentionByName(shieldConfig.retentionName)
        if (retention == null) {
            throw new RuntimeException("Retention ${shieldConfig.retentionName} that is configured does not exist on shield")
        }
        ScheduleDto schedule = buildClient().getScheduleByName(shieldConfig.scheduleName)
        if (schedule == null) {
            throw new RuntimeException("Schedule ${shieldConfig.scheduleName} that is configured does not exist on shield")
        }

        createOrUpdateJob(serviceInstanceId, targetUuid, store.uuid, retention.uuid, schedule.uuid)
    }

    private String createOrUpdateTarget(String serviceInstanceId, ShieldTarget target) {
        String targetName = targetName(serviceInstanceId)
        TargetDto targetOnShield = buildClient().getTargetByName(targetName)
        targetOnShield == null ? buildClient().createTarget(targetName, target) : buildClient().updateTarget(targetOnShield, target)
    }

    private String createOrUpdateJob(String serviceInstanceId,
                                     String targetUuid,
                                     String storeUuid,
                                     String retentionUuid,
                                     String scheduleUuid,
                                     boolean paused = true) {
        String jobName = jobName(serviceInstanceId)
        JobDto jobOnShield = buildClient().getJobByName(jobName)
        jobOnShield == null ?
                buildClient().createJob(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused) :
                buildClient().updateJob(jobOnShield, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
    }

    private String jobName(String serviceInstanceId) {
        "${shieldConfig.jobPrefix}${serviceInstanceId}"
    }

    private String targetName(String serviceInstanceId) {
        "${shieldConfig.targetPrefix}${serviceInstanceId}"
    }

    private ShieldRestClient buildClient() {
        shieldRestClientFactory.build(new RestTemplate(), shieldConfig.baseUrl, shieldConfig.apiKey, shieldConfig.agent)
    }
}
