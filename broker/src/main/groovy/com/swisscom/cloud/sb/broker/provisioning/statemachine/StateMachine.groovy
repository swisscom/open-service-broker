/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic

@CompileStatic
class StateMachine {
    private final List<ServiceStateWithAction> stateList = new ArrayList<ServiceStateWithAction>()
    private ServiceStateWithAction currentState

    StateMachine(List<? extends ServiceStateWithAction> states) {
        stateList.addAll(states)
    }

    StateMachine addState(ServiceStateWithAction serviceState, Integer index = null) {
        Preconditions.checkNotNull(serviceState)
        if (index != null) {
            stateList.add(index, serviceState)
        } else {
            stateList.add(serviceState)
        }
        return this
    }

    synchronized StateChangeActionResult setCurrentState(ServiceStateWithAction serviceState, StateMachineContext context) {
        Preconditions.checkNotNull(serviceState,'ServiceState can not be null')
        Preconditions.checkArgument(states.contains(serviceState),"Invalid state:${serviceState.toString()}")
        currentState = serviceState

        return currentState.triggerAction(context)
    }

    synchronized ServiceStateWithAction nextState(ServiceStateWithAction state) {
        Iterator<ServiceStateWithAction> it = stateList.iterator()
        while (it.hasNext()) {
            ServiceState current = it.next()
            if (current == state) {
                if (!it.hasNext())
                    throw new RuntimeException("Current state:${current.toString()} is the final state!")
                return it.next()
            }
        }

        throw new RuntimeException("StateMachine is in invalid State.")
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
    String toString() {
        return "StateMachine{" + "stateList=" + stateList.join(',') + '}'
    }
}
