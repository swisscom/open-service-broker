package com.swisscom.cloud.sb.broker.services.statemachine

import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.services.credential.BindRequest
import com.swisscom.cloud.sb.broker.services.credential.BindResponse
import com.swisscom.cloud.sb.broker.services.credential.UnbindRequest
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachine
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineBasedServiceProvider
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext
import org.springframework.stereotype.Component

@Component
class DummyStateMachineBasedServiceProvider extends StateMachineBasedServiceProvider<DummyAsyncServiceConfig> {
    DummyStateMachineBasedServiceProvider(AsyncProvisioningService asyncProvisioningService,
                                          ProvisioningPersistenceService provisioningPersistenceService,
                                          DummyAsyncServiceConfig serviceConfig) {
        super(asyncProvisioningService, provisioningPersistenceService, serviceConfig)
    }

    @Override
    protected StateMachine getProvisionStateMachine(LastOperationJobContext lastOperationJobContext) {
        return null
    }

    @Override
    protected StateMachine getDeprovisionStateMachine(LastOperationJobContext lastOperationJobContext) {
        return null
    }

    @Override
    protected StateMachine getUpdateStateMachine(LastOperationJobContext lastOperationJobContext) {
        return null
    }

    @Override
    protected StateMachineContext createStateMachineContext(LastOperationJobContext lastOperationJobContext) {
        return null
    }

    @Override
    BindResponse bind(BindRequest request) {
        return null
    }

    @Override
    void unbind(UnbindRequest request) {

    }
}
