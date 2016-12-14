package com.swisscom.cf.broker.backup.job.config

import com.swisscom.cf.broker.async.job.AbstractJob
import com.swisscom.cf.broker.async.job.JobConfig
import com.swisscom.cf.broker.model.Restore
import groovy.transform.CompileStatic

@CompileStatic
class RestoreJobConfig extends JobConfig {
    final Restore restore

    RestoreJobConfig(Class<? extends AbstractJob> jobClass, String guid, int retryIntervalInSeconds, double maxRetryDurationInMinutes, Restore restore) {
        super(jobClass, guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.restore = restore
    }
}
