package com.swisscom.cf.broker.backup.job.config

import com.swisscom.cf.broker.async.job.AbstractJob
import com.swisscom.cf.broker.async.job.JobConfig
import com.swisscom.cf.broker.model.Backup
import groovy.transform.CompileStatic

@CompileStatic
class BackupCreationJobConfig extends JobConfig {
    final Backup backup

    BackupCreationJobConfig(Class<? extends AbstractJob> jobClass, String guid, int retryIntervalInSeconds, double maxRetryDurationInMinutes, Backup backup) {
        super(jobClass, guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.backup = backup
    }
}