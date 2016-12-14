package com.swisscom.cf.broker.provisioning.job

import com.swisscom.cf.broker.async.job.AbstractJob
import com.swisscom.cf.broker.async.job.JobConfig
import com.swisscom.cf.broker.model.ProvisionRequest
import groovy.transform.CompileStatic

@CompileStatic
class ProvisioningjobConfig extends JobConfig {

    final ProvisionRequest provisionRequest

    ProvisioningjobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, provisionRequest.serviceInstanceGuid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.provisionRequest = provisionRequest
    }

    ProvisioningjobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest) {
        this(jobClass, provisionRequest, JobConfig.RETRY_INTERVAL_IN_SECONDS, JobConfig.MAX_RETRY_DURATION_IN_MINUTES)
    }

}
