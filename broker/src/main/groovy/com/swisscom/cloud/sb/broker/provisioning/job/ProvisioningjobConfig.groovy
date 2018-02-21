package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import groovy.transform.CompileStatic

@CompileStatic
class ProvisioningjobConfig extends JobConfig {

    final ProvisionRequest provisionRequest

    ProvisioningjobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest, int retryIntervalInSeconds, double maxRetryDurationInMinutes, int delayInSeconds = JobConfig.NO_DELAY) {
        super(jobClass, provisionRequest.serviceInstanceGuid, retryIntervalInSeconds, maxRetryDurationInMinutes, delayInSeconds)
        this.provisionRequest = provisionRequest
    }

    ProvisioningjobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest) {
        this(jobClass, provisionRequest, JobConfig.RETRY_INTERVAL_IN_SECONDS, JobConfig.MAX_RETRY_DURATION_IN_MINUTES, JobConfig.NO_DELAY)
    }

}
