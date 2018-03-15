package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractJob
import com.swisscom.cloud.sb.broker.async.job.JobConfig
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import groovy.transform.CompileStatic

@CompileStatic
class ProvisioningJobConfig extends JobConfig {

    final ProvisionRequest provisionRequest

    ProvisioningJobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest, int retryIntervalInSeconds, double maxRetryDurationInMinutes) {
        super(jobClass, provisionRequest.serviceInstanceGuid, retryIntervalInSeconds, maxRetryDurationInMinutes)
        this.provisionRequest = provisionRequest
    }

    ProvisioningJobConfig(Class<? extends AbstractJob> jobClass, ProvisionRequest provisionRequest) {
        this(jobClass, provisionRequest, JobConfig.RETRY_INTERVAL_IN_SECONDS, JobConfig.MAX_RETRY_DURATION_IN_MINUTES)
    }

}
