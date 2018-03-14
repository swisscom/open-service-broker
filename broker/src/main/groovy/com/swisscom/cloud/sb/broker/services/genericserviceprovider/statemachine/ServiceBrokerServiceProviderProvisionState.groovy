package com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp

enum ServiceBrokerServiceProviderProvisionState implements ServiceStateWithAction<ServiceBrokerServiceProviderStateMachineContext> {
    PROVISION_IN_PROGRESS(LastOperation.Status.IN_PROGRESS, new OnStateChange<ServiceBrokerServiceProviderStateMachineContext>(){
        @Override
        StateChangeActionResult triggerAction(ServiceBrokerServiceProviderStateMachineContext context) {
            new StateChangeActionResult(go2NextState: true)
        }
    }),

    PROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp()),

    PROVISION_FAILED(LastOperation.Status.FAILED, new NoOp())

    private final LastOperation.Status status
    private final OnStateChange<ServiceBrokerServiceProviderStateMachineContext> onStateChange

    ServiceBrokerServiceProviderProvisionState(LastOperation.Status status, OnStateChange<ServiceBrokerServiceProviderStateMachineContext> onStateChange) {
        this.status = status
        this.onStateChange = onStateChange
    }

    @Override
    StateChangeActionResult triggerAction(ServiceBrokerServiceProviderStateMachineContext context) {
        onStateChange.triggerAction(context)
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }
}