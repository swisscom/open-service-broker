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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DummyAsyncServiceProvider extends AsyncServiceProvider<DummyAsyncServiceConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyAsyncServiceConfig.class)

    DummyAsyncServiceProvider(AsyncProvisioningService asyncProvisioningService,
                              ProvisioningPersistenceService provisioningPersistenceService,
                              DummyAsyncServiceConfig serviceConfig) {
        super(asyncProvisioningService, provisioningPersistenceService, serviceConfig)
    }

    @Override
    BindResponse bind(BindRequest request) {
        LOGGER.debug("bind({})", request)
        return null
    }

    @Override
    void unbind(UnbindRequest request) {
        LOGGER.debug("unbind({})", request)
    }

    @Override
    AsyncOperationResult requestUpdate(LastOperationJobContext context) {
        LOGGER.debug("requestUpdate({})", context)
        return null
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        LOGGER.debug("requestDeprovision({})", context)
        return null
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        LOGGER.debug("requestProvision({})", context)
        return null
    }
}
