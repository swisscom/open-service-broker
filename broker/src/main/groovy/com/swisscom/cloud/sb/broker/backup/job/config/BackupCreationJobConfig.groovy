package com.swisscom.cloud.sb.broker.backup.job.config

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.model.Backup
import groovy.transform.CompileStatic

@CompileStatic
class BackupCreationJobConfig extends JobConfig {
    final Backup backup

    BackupCreationJobConfig(Class<? extends AbstractJob> jobClass, String guid, int retryIntervalInSeconds, double maxRetryDurationInMinutes, Backup backup) {
        super(jobClass, guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.backup = backup
    }
}