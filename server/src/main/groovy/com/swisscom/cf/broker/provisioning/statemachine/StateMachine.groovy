package com.swisscom.cf.broker.provisioning.statemachine

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic

@CompileStatic
class StateMachine {
    private final List stateList = new ArrayList<ServiceStateWithAction>()
    private ServiceStateWithAction currentState

    StateMachine(List<? extends ServiceStateWithAction> states) {
        stateList.addAll(states)
    }

    StateMachine addState(ServiceStateWithAction serviceState) {
        Preconditions.checkNotNull(serviceState)
        stateList.add(serviceState)
        return this
    }

    synchronized StateChangeActionResult setCurrentState(ServiceStateWithAction serviceState,StateMachineContext context){
        Preconditions.checkNotNull(serviceState,'ServiceState can not be null')
        Preconditions.checkArgument(states.contains(serviceState),"Invalid state:${serviceState.toString()}")
        currentState = serviceState

        return currentState.triggerAction(context)
    }

    synchronized ServiceState nextState(ServiceStateWithAction state) {
        Iterator<ServiceState> it = stateList.iterator()
        while (it.hasNext()) {
            ServiceState current = it.next()
            if (current == state) {
                if(!it.hasNext()){throw new RuntimeException("Current state:${current.toString()} is the final state!")}
                return it.next()
            }
        }
    }

    StateMachine addAllFromStateMachine(StateMachine stateMachine) {
        addAll(stateMachine.states)
    }

    StateMachine addAll(List<? extends ServiceStateWithAction> states){
        stateList.addAll(states)
        return this
    }

    List<ServiceStateWithAction> getStates() {
        return Collections.unmodifiableList(stateList)
    }

    @Override
    public String toString() {
        return "StateMachine{" + "stateList=" + stateList.join(',') + '}'
    }
}
