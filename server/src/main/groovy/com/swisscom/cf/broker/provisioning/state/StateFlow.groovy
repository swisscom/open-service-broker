package com.swisscom.cf.broker.provisioning.state

import groovy.transform.CompileStatic

import static java.util.Optional.of


@CompileStatic
class StateFlow {
    private LinkedHashMap states = new LinkedHashMap<ServiceState,OnStateChange> ()

    StateFlow withStateAndAction(ServiceState serviceState, OnStateChange action ){
        states.put(serviceState,action)
        return this
    }

    Optional<ServiceState> nextState(ServiceState state){
        def it =  states.keySet().iterator()
        while (it.hasNext()){
            def current = it.next()
            if(current == state){
                return of( it.next() as ServiceState)
            }
        }
        return Optional.empty()
    }

    OnStateChange getAction(ServiceState serviceState){
        return states.get(serviceState)
    }

    StateFlow addFlow(StateFlow flow) {
        states.putAll(flow.states)
        return this
    }

    LinkedHashMap<ServiceState,OnStateChange> getStates(){
        return states
    }
}
