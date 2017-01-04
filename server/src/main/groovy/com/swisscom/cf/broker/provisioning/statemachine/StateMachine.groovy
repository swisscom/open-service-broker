package com.swisscom.cf.broker.provisioning.statemachine

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class StateMachine {
    private final List states = new ArrayList<ServiceStateWithAction>()
    private ServiceStateWithAction currentState

    StateMachine addState(ServiceStateWithAction serviceState) {
        Preconditions.checkNotNull(serviceState)
        states.add(serviceState)
        return this
    }

    synchronized StateChangeActionResult setCurrentState(ServiceStateWithAction serviceState,StateMachineContext context){
        Preconditions.checkNotNull(serviceState,'ServiceState can not be null')
        Preconditions.checkArgument(states.contains(serviceState),"Invalid state:${serviceState.toString()}")
        currentState = serviceState

        return currentState.triggerAction(context)
    }

    synchronized ServiceState nextState(ServiceStateWithAction state) {
        Iterator<ServiceState> it = states.iterator()
        while (it.hasNext()) {
            ServiceState current = it.next()
            if (current == state) {
                if(!it.hasNext()){throw new RuntimeException("Current state:${current.toString()} is the final state!")}
                return it.next()
            }
        }
    }

    StateMachine addAllFromStateMachine(StateMachine stateMachine) {
        states.addAll(stateMachine.states)
        return this
    }

    List<ServiceStateWithAction> getStates() {
        return Collections.unmodifiableList(states)
    }

    @Override
    public String toString() {
        return "StateMachine{" + "states=" + states.join(',') + '}'
    }
}
