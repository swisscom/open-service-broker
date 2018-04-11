package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider

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

    protected abstract StateMachine getProvisionStateMachine()

    protected abstract StateMachine getDeprovisionStateMachine()

    protected abstract StateMachine getUpdateStateMachine()

    private StateMachine getStateMachineForOperation(LastOperation.Operation operation) {
        switch (operation) {
            case LastOperation.Operation.DEPROVISION:
                return getDeprovisionStateMachine()
            case LastOperation.Operation.PROVISION:
                return getProvisionStateMachine()
            case LastOperation.Operation.UPDATE:
                return getUpdateStateMachine()
        }
    }

    protected ServiceStateWithAction getProvisionInitialState(LastOperationJobContext lastOperationJobContext) {
        getStateMachineForOperation(LastOperation.Operation.PROVISION).states.first()
    }

    protected ServiceStateWithAction getDeprovisionInitialState(LastOperationJobContext lastOperationJobContext) {
        getStateMachineForOperation(LastOperation.Operation.DEPROVISION).states.first()
    }

    protected ServiceStateWithAction getUpdateInitialState(LastOperationJobContext lastOperationJobContext) {
        getStateMachineForOperation(LastOperation.Operation.UPDATE).states.first()
    }

    private ServiceStateWithAction getInitialState(LastOperationJobContext lastOperationJobContext, LastOperation.Operation operation) {
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

    private AsyncOperationResult ExecuteStateTransition(LastOperationJobContext lastOperationJobContext, LastOperation.Operation operation) {
        def stateMachine = getStateMachineForOperation(operation)
        def currentState = getCurrentStateForContext(stateMachine, lastOperationJobContext, operation)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(lastOperationJobContext))

        AsyncOperationResult.of(
                actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState,
                actionResult.details,
                actionResult.message)
    }

    private ServiceStateWithAction getCurrentStateForContext(
            StateMachine stateMachine,
            LastOperationJobContext context,
            LastOperation.Operation operation) {
        String stateName = context.lastOperation.internalState

        if (!stateName)
            return getInitialState(context, operation)

        stateMachine.states.find { it -> it.serviceInternalState == context.lastOperation.internalState }
    }
}
