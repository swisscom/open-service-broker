package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.Context
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

    ProvisionResponse provision(ProvisionRequest provisionRequest, Context context = null) {
        log.trace("Provision request:${provisionRequest.toString()}")
        handleAsyncClientRequirement(provisionRequest.plan, provisionRequest.acceptsIncomplete)
        def instance = provisioningPersistenceService.createServiceInstance(provisionRequest)
        ProvisionResponse provisionResponse = serviceProviderLookup.findServiceProvider(provisionRequest.plan).provision(provisionRequest)
        instance = provisioningPersistenceService.updateServiceInstanceCompletion(instance, !provisionResponse.isAsync)
        instance = provisioningPersistenceService.updateServiceDetails(provisionResponse.details, instance)
        provisioningPersistenceService.createServiceContext(context, instance)
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
