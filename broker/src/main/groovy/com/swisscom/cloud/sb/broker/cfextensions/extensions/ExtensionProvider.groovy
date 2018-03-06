package com.swisscom.cloud.sb.broker.cfextensions.extensions

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.backup.shield.dto.JobStatus
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired

@Slf4j
trait ExtensionProvider{
//
//    @Autowired
//    protected JobManager jobManager

    abstract Collection<Extension> buildExtensions()

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