package com.swisscom.cloud.sb.broker.provisioning.job

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.async.job.AbstractLastOperationJob
import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.DeprovisionRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
//When renaming, the existing jobs in Quartz DB should be renamed accordingly!!!
class ServiceDeprovisioningJob extends AbstractLastOperationJob {
    @Autowired
    private ServiceProviderLookup serviceProviderLookup
    @Autowired
    private JobManager jobManager
    @Autowired
    private DeprovisionRequestRepository deprovisionRequestRepository

    protected LastOperationJobContext enrichContext(LastOperationJobContext context) {
        String serviceInstanceGuid = context.lastOperation.guid
        DeprovisionRequest deprovisionRequest = deprovisionRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
        context.deprovisionRequest = deprovisionRequest
        context.serviceInstance = deprovisionRequest.serviceInstance
        return context
    }

    @Override
    protected AsyncOperationResult handleJob(LastOperationJobContext context) {
        log.info("About to request service deprovisioning, ${context.lastOperation.toString()}")
        AsyncServiceDeprovisioner serviceDeprovisioner = ((AsyncServiceDeprovisioner) serviceProviderLookup.findServiceProvider(context.serviceInstance.plan))
        Optional<AsyncOperationResult> result = serviceDeprovisioner.requestDeprovision(context)
        AsyncOperationResult jobResult
        if (result.isPresent()) {
            provisioningPersistenceService.updateServiceDetails(result.get().details, context.serviceInstance)
            jobResult = result.get()
        } else {
            jobResult = new AsyncOperationResult(status: LastOperation.Status.SUCCESS)
        }

        if (jobResult.status == LastOperation.Status.SUCCESS) {
            provisioningPersistenceService.markServiceInstanceAsDeleted(context.serviceInstance)
            provisioningPersistenceService.removeDeprovisionRequestIfExists(context.lastOperation.guid)
        }
        return jobResult
    }
}
