package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider
import groovy.transform.CompileStatic

@CompileStatic
abstract class StateMachineBasedServiceProvider<T extends AsyncServiceConfig> extends AsyncServiceProvider<T> {

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext lastOperationJobContext) {
        ExecuteStateTransition(lastOperationJobContext, LastOperation.Operation.PROVISION)
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext lastOperationJobContext) {
        Optional.of(ExecuteStateTransition(lastOperationJobContext, LastOperation.Operation.DEPROVISION))
    }

    @Override
    AsyncOperationResult requestUpdate(LastOperationJobContext lastOperationJobContext) {
        ExecuteStateTransition(lastOperationJobContext, LastOperation.Operation.UPDATE)
    }

    protected abstract StateMachine getProvisionStateMachine(LastOperationJobContext lastOperationJobContext)

    protected abstract StateMachine getDeprovisionStateMachine(LastOperationJobContext lastOperationJobContext)

    protected abstract StateMachine getUpdateStateMachine(LastOperationJobContext lastOperationJobContext)

    protected StateMachine getStateMachineForOperation(LastOperationJobContext lastOperationJobContext, LastOperation.Operation operation) {
        switch (operation) {
            case LastOperation.Operation.DEPROVISION:
                return getDeprovisionStateMachine(lastOperationJobContext)
            case LastOperation.Operation.PROVISION:
                return getProvisionStateMachine(lastOperationJobContext)
            case LastOperation.Operation.UPDATE:
                return getUpdateStateMachine(lastOperationJobContext)
        }
    }

    protected ServiceStateWithAction getProvisionInitialState(LastOperationJobContext lastOperationJobContext) {
        getStateMachineForOperation(lastOperationJobContext, LastOperation.Operation.PROVISION).states.first()
    }

    protected ServiceStateWithAction getDeprovisionInitialState(LastOperationJobContext lastOperationJobContext) {
        getStateMachineForOperation(lastOperationJobContext, LastOperation.Operation.DEPROVISION).states.first()
    }

    protected ServiceStateWithAction getUpdateInitialState(LastOperationJobContext lastOperationJobContext) {
        getStateMachineForOperation(lastOperationJobContext, LastOperation.Operation.UPDATE).states.first()
    }

    protected ServiceStateWithAction getInitialState(LastOperationJobContext lastOperationJobContext, LastOperation.Operation operation) {
        switch (operation) {
            case LastOperation.Operation.DEPROVISION:
                return getDeprovisionInitialState(lastOperationJobContext)
            case LastOperation.Operation.PROVISION:
                return getProvisionInitialState(lastOperationJobContext)
            case LastOperation.Operation.UPDATE:
                return getUpdateInitialState(lastOperationJobContext)
        }
    }

    protected abstract StateMachineContext createStateMachineContext(LastOperationJobContext lastOperationJobContext)

    protected AsyncOperationResult ExecuteStateTransition(LastOperationJobContext lastOperationJobContext, LastOperation.Operation operation) {
        def stateMachine = getStateMachineForOperation(lastOperationJobContext, operation)
        def currentState = getCurrentStateForContext(stateMachine, lastOperationJobContext, operation)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(lastOperationJobContext))

        AsyncOperationResult.of(
                actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState,
                actionResult.details,
                actionResult.message)
    }

    protected ServiceStateWithAction getCurrentStateForContext(
            StateMachine stateMachine,
            LastOperationJobContext context,
            LastOperation.Operation operation) {
        String stateName = context.lastOperation.internalState

        if (!stateName)
            return getInitialState(context, operation)

        stateMachine.states.find { it -> it.serviceInternalState == context.lastOperation.internalState }
    }
}
