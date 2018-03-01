package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.backup.shield.dto.JobStatus
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import groovy.util.logging.Slf4j

@Slf4j
trait ExtensionProvider{

    abstract Extension buildExtension()

    abstract TaskDto getTask(String taskUuid)

    JobStatus getJobStatus(TaskDto task) {
        if (task.statusParsed.isRunning()) {
            return JobStatus.RUNNING
        }
        if (task.statusParsed.isFailed()) {
            log.warn("Task failed: ${task}")
            return JobStatus.FAILED
        }
        if (task.statusParsed.isDone()) {
            return JobStatus.SUCCESSFUL
        }
        throw new RuntimeException("Invalid task status ${task.status} for task ${task.job_uuid}")
    }
}