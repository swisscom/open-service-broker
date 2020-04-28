package com.swisscom.cloud.sb.broker

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider
import com.swisscom.cloud.sb.broker.updating.UpdatingPersistenceService
import org.springframework.stereotype.Component

@Component
class DummyAsyncServiceProvider extends AsyncServiceProvider<DummyAsyncServiceConfig> {
    private UpdatingPersistenceService updatingPersistenceService

    DummyAsyncServiceProvider(AsyncProvisioningService asyncProvisioningService,
                              ProvisioningPersistenceService provisioningPersistenceService,
                              DummyAsyncServiceConfig serviceConfig,
                              UpdatingPersistenceService updatingPersistenceService) {
        super(asyncProvisioningService, provisioningPersistenceService, serviceConfig)
        this.updatingPersistenceService = updatingPersistenceService
    }

    @Override
    BindResponse bind(BindRequest request) {
        return null
    }

    @Override
    void unbind(UnbindRequest request) {

    }

    @Override
    AsyncOperationResult requestUpdate(LastOperationJobContext context) {
        updatingPersistenceService.updatePlan(context.getServiceInstance(),
                                              context.getUpdateRequest().getParameters(),
                                              context.getUpdateRequest().getPlan(),
                                              context.getUpdateRequest().getServiceContext())
        return null
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        return null
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        return null
    }
}
