package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractLastOperationJob
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceUpdater
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Slf4j
public class ServiceUpdateJob extends AbstractLastOperationJob {
    @Autowired
    private ServiceProviderLookup serviceProviderLookup
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private UpdateRequestRepository updateRequestRepository

    protected LastOperationJobContext enrichContext(LastOperationJobContext jobContext) {
        log.info("About to update service instance, ${jobContext.lastOperation.toString()}")
        def serviceInstanceGuid = jobContext.lastOperation.guid
        jobContext.serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        jobContext.plan = jobContext.serviceInstance.plan
        jobContext.updateRequest = updateRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
                .sort({it -> it.dateCreated})
                .reverse()
                .first()
        return jobContext
    }

    @Override
    protected AsyncOperationResult handleJob(LastOperationJobContext context) {
        log.info("About to update service instance, ${context.lastOperation.toString()}")
        return ((AsyncServiceUpdater)serviceProviderLookup.findServiceProvider(context.serviceInstance.plan)).requestUpdate(context)
    }
}
