package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Slf4j
@Service
class UpdatingService {
    protected ServiceProviderLookup serviceProviderLookup
    protected UpdatingPersistenceService updatingPersistenceService

    @Autowired
    public UpdatingService(ServiceProviderLookup serviceProviderLookup, UpdatingPersistenceService updatingPersistenceService) {
        this.serviceProviderLookup = serviceProviderLookup
        this.updatingPersistenceService = updatingPersistenceService
    }

    UpdateResponse update(ServiceInstance serviceInstance, UpdateRequest updateRequest, boolean acceptsIncomplete) {
        if (isPlanChanging(updateRequest))
            handleAsyncClientRequirement(updateRequest.plan, acceptsIncomplete)
        updatingPersistenceService.saveUpdateRequest(updateRequest)
        def response = serviceProviderLookup.findServiceProvider(updateRequest.previousPlan).update(updateRequest)
        updatingPersistenceService.updatePlanAndServiceDetails(serviceInstance, updateRequest.parameters, response.details, updateRequest.plan)
        return response
    }

    boolean isPlanChanging(UpdateRequest updateRequest) {
        return updateRequest.plan != null &&
                updateRequest.previousPlan != null &&
                updateRequest.plan.guid != updateRequest.previousPlan.guid
    }

    private static void handleAsyncClientRequirement(Plan plan, boolean acceptsIncomplete) {
        if ((plan.service.asyncRequired || plan.asyncRequired) && !acceptsIncomplete) {
            ErrorCode.ASYNC_REQUIRED.throwNew()
        }
    }
}
