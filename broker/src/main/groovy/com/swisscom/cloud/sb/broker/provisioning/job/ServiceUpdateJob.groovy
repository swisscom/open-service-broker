package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractLastOperationJob
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceUpdater
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import org.springframework.beans.factory.annotation.Autowired

class ServiceUpdateJob extends AbstractLastOperationJob {
    private ServiceProviderLookup serviceProviderLookup

    @Autowired
    ServiceUpdateJob(ServiceProviderLookup serviceProviderLookup)
    {
        this.serviceProviderLookup = serviceProviderLookup
    }

    @Override
    protected AsyncOperationResult handleJob(LastOperationJobContext context) {
        log.info("About to update service instance, ${context.lastOperation.toString()}")
        def asyncServiceUpdater = ((AsyncServiceUpdater)serviceProviderLookup.findServiceProvider(context.serviceInstance.plan))
        def result = asyncServiceUpdater.requestUpdate(context)
        AsyncOperationResult jobResult
        if (result.isPresent()) {
            jobResult = result.get()
        } else {
            jobResult = new AsyncOperationResult(status: LastOperation.Status.SUCCESS)
        }

        return jobResult
    }
}
