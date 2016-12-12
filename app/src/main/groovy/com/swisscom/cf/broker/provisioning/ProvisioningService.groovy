package com.swisscom.cf.broker.provisioning

import com.swisscom.cf.broker.exception.ErrorCode
import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.ProvisionRequest
import com.swisscom.cf.broker.services.common.DeprovisionResponse
import com.swisscom.cf.broker.services.common.ProvisionResponse
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Log4j
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
        ProvisionResponse provisionResponse = serviceProviderLookup.findServiceProvider(provisionRequest.plan).provision(provisionRequest)
        if (!provisionResponse.isAsync) {
            provisioningPersistenceService.createServiceInstance(provisionRequest, provisionResponse)
        }
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
