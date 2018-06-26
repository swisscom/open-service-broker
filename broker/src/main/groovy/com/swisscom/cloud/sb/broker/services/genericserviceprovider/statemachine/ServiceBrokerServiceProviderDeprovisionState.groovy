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

package com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceState
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp

enum ServiceBrokerServiceProviderDeprovisionState implements ServiceStateWithAction<ServiceBrokerServiceProviderStateMachineContext> {
    DEPROVISION_IN_PROGRESS(LastOperation.Status.IN_PROGRESS, new OnStateChange<ServiceBrokerServiceProviderStateMachineContext>() {
        @Override
        StateChangeActionResult triggerAction(ServiceBrokerServiceProviderStateMachineContext context) {
            new StateChangeActionResult(go2NextState: context.sbspFacade.deprovisionServiceInstance(context.lastOperationJobContext.serviceInstance))
        }
    }),

    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp()),

    DEPROVISION_FAILED(LastOperation.Status.FAILED, new NoOp())

    private final LastOperation.Status status
    private final OnStateChange<ServiceBrokerServiceProviderStateMachineContext> onStateChange

    ServiceBrokerServiceProviderDeprovisionState(LastOperation.Status status, OnStateChange<ServiceBrokerServiceProviderStateMachineContext> onStateChange) {
        this.status = status
        this.onStateChange = onStateChange
    }

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + ServiceBrokerServiceProviderDeprovisionState.values()) {
            map.put(serviceState.getServiceInternalState(), serviceState)
        }
    }

    static ServiceStateWithAction of(String state) {
        map.get(state)
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
