package com.swisscom.cloud.sb.broker.provisioning.job

import com.swisscom.cloud.sb.broker.async.job.AbstractLastOperationJob
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.repository.ProvisionRequestRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Slf4j
//When renaming, the existing jobs in Quartz DB should be renamed accordingly!!!
class ServiceProvisioningJob extends AbstractLastOperationJob {
    @Autowired
    private ServiceProviderLookup serviceProviderLookup
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ProvisionRequestRepository provisionRequestRepository

    protected LastOperationJobContext enrichContext(LastOperationJobContext jobContext) {
        String serviceInstanceGuid = jobContext.lastOperation.guid
        ProvisionRequest provisionRequest = provisionRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
        jobContext.provisionRequest = provisionRequest
        jobContext.plan = provisionRequest.plan
        jobContext.serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        return jobContext
    }

    @Override
    protected AsyncOperationResult handleJob(LastOperationJobContext jobContext) {
        log.info("About to request service provision, ${jobContext.lastOperation.toString()}")
        AsyncOperationResult provisionResult = findServiceProvisioner(jobContext).requestProvision(jobContext)
        boolean parentInstanceExists = checkServiceInstanceByParentAlias(jobContext.provisionRequest)
        if (parentInstanceExists) {
            jobContext.serviceInstance = provisioningPersistenceService.createServiceInstanceOrUpdateDetails(jobContext.provisionRequest, new ProvisionResponse(details: provisionResult.details, isAsync: true))
            if (provisionResult.status == LastOperation.Status.SUCCESS) {
                provisioningPersistenceService.updateServiceInstanceCompletion(jobContext.serviceInstance, true)
            }
        } else {
            provisionResult.status = jobContext.lastOperation.status
        }
        return provisionResult
    }

    private boolean checkServiceInstanceByParentAlias(ProvisionRequest provisionRequest) {
        if (!provisionRequest.parameters) {
            return true
        }
        def jsonSlurper = new JsonSlurper()
        def parametersMap = jsonSlurper.parseText(provisionRequest.parameters) as Map
        def parentAlias = parametersMap?.parentAlias as String
        if (!parentAlias) {
            return true
        }
        def details = serviceInstanceRepository.findByDetails_KeyAndDetails_Value(ServiceDetailType.ALIAS.name(), parentAlias)
        return details != null && details.size() > 0
    }
    
    private AsyncServiceProvisioner findServiceProvisioner(LastOperationJobContext context) {
        AsyncServiceProvisioner serviceProvisioner = ((AsyncServiceProvisioner) serviceProviderLookup.findServiceProvider(context.plan))
        return serviceProvisioner
    }
}


