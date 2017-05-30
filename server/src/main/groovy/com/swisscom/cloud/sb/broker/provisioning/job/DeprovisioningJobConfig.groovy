package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import groovy.transform.CompileStatic

@CompileStatic
class DeprovisioningJobConfig extends JobConfig {
    final DeprovisionRequest deprovisionRequest

    DeprovisioningJobConfig(Class<? extends AbstractJob> jobClass, DeprovisionRequest deprovisionRequest, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, deprovisionRequest.serviceInstance.guid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.deprovisionRequest = deprovisionRequest
    }

    DeprovisioningJobConfig(Class<? extends AbstractJob> jobClass, DeprovisionRequest deprovisionRequest) {
        this(jobClass, deprovisionRequest, JobConfig.RETRY_INTERVAL_IN_SECONDS, JobConfig.MAX_RETRY_DURATION_IN_MINUTES)
    }
}