package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.context.CloudFoundryContextRestrictedOnly
import com.swisscom.cloud.sb.broker.util.servicecontext.ServiceContextHelper
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class ProvisioningService {
    @Autowired
    protected ServiceProviderLookup serviceProviderLookup
    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService
    //TODO
    /*@Autowired
    BackupService backupService*/

    ProvisionResponse provision(ProvisionRequest provisionRequest) {
        log.trace("Provision request:${provisionRequest.toString()}")
        handleAsyncClientRequirement(provisionRequest.plan, provisionRequest.acceptsIncomplete)
        def instance = provisioningPersistenceService.createServiceInstance(provisionRequest)
        def serviceProvider = serviceProviderLookup.findServiceProvider(provisionRequest.plan)

        def context = ServiceContextHelper.convertFrom(provisionRequest.serviceContext)
        if (provisionRequest.serviceContext && serviceProvider instanceof CloudFoundryContextRestrictedOnly && !(context instanceof CloudFoundryContext)) {
            ErrorCode.CLOUDFOUNDRY_CONTEXT_REQUIRED.throwNew()
        }

        ProvisionResponse provisionResponse = serviceProvider.provision(provisionRequest)
        instance = provisioningPersistenceService.updateServiceInstanceCompletion(instance, !provisionResponse.isAsync)
        provisioningPersistenceService.updateServiceDetails(provisionResponse.details, instance)
        return provisionResponse
    }

    private static void handleAsyncClientRequirement(Plan plan, boolean acceptsIncomplete) {
        if ((plan.service.asyncRequired || plan.asyncRequired) && !acceptsIncomplete) {
            ErrorCode.ASYNC_REQUIRED.throwNew()
        }
    }

    DeprovisionResponse deprovision(DeprovisionRequest deprovisionRequest) {
        log.trace("DeprovisionRequest request:${deprovisionRequest.toString()}")
        handleAsyncClientRequirement(deprovisionRequest.serviceInstance.plan, deprovisionRequest.acceptsIncomplete)
/*
        if(backupService.isBackupEnabled(deprovisionRequest.serviceInstance)) {
            backupService.notifyServiceInstanceDeletion(deprovisionRequest.serviceInstance)
        }*/
        DeprovisionResponse response = serviceProviderLookup.findServiceProvider(deprovisionRequest.serviceInstance.plan).deprovision(deprovisionRequest)
        if (!response.isAsync) {
            provisioningPersistenceService.markServiceInstanceAsDeleted(deprovisionRequest.serviceInstance)
        }
        return response
    }
}
