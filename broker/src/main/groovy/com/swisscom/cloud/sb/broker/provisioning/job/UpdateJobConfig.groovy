package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import groovy.transform.CompileStatic

@CompileStatic
class UpdateJobConfig extends JobConfig {
    final UpdateRequest updateRequest

    UpdateJobConfig(Class<? extends AbstractJob> jobClass, UpdateRequest updateRequest, String guid, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.updateRequest = updateRequest
    }
}
