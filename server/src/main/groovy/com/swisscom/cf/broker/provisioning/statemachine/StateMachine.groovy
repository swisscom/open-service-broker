package com.swisscom.cf.broker.provisioning.statemachine

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class StateMachine {
    private final Map states = new LinkedHashMap<ServiceState, OnStateChange>()
    private ServiceState currentState

    StateMachine withStateAndAction(ServiceState serviceState, OnStateChange action) {
        Preconditions.checkNotNull(serviceState)
        Preconditions.checkNotNull(action)

        states.put(serviceState, action)
        return this
    }

    synchronized StateChangeActionResult setCurrentState(ServiceState serviceState,StateMachineContext context){
        Preconditions.checkNotNull(serviceState,'ServiceState can not be null')
        Preconditions.checkArgument(states.keySet().contains(serviceState),"Invalid state:${serviceState.toString()}")
        currentState = serviceState

        return getAction(currentState).triggerAction(context)
    }

    synchronized ServiceState nextState(ServiceState state) {
        Iterator<ServiceState> it = states.keySet().iterator()
        while (it.hasNext()) {
            ServiceState current = it.next()
            if (current == state) {
                if(!it.hasNext()){throw new RuntimeException("Current state:${current.toString()} is the final state!")}
                return it.next()
            }
        }
    }

    private OnStateChange getAction(ServiceState serviceState) {
        return states.get(serviceState)
    }

    StateMachine addAllFromStateMachine(StateMachine stateMachine) {
        states.putAll(stateMachine.states)
        return this
    }

    Map<ServiceState, OnStateChange> getStates() {
        return Collections.unmodifiableMap(states)
    }

    @Override
    public String toString() {
        return "StateMachine{" + "states=" + states.keySet().join(',') + '}'
    }
}
