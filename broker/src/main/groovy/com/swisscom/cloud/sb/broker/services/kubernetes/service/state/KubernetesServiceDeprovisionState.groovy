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

package com.swisscom.cloud.sb.broker.services.kubernetes.service.state

import com.swisscom.cloud.sb.broker.backup.SystemBackupProvider
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
enum KubernetesServiceDeprovisionState implements ServiceStateWithAction<KubernetesServiceStateMachineContext> {

    KUBERNETES_NAMESPACE_DELETION(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            stateContext.kubernetesFacade.deprovision(stateContext.lastOperationJobContext.deprovisionRequest)
            return new StateChangeActionResult(go2NextState: true)
        }
    }),

    CHECK_NAMESPACE_DELETION_SUCCESSFUL(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: stateContext.kubernetesFacade.isKubernetesNamespaceDeleted(stateContext.lastOperationJobContext.deprovisionRequest.serviceInstanceGuid))
        }
    }),

    UNREGISTER_SHIELD_SYSTEM_BACKUP(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            try {
                def facadeWithBackup = stateContext.kubernetesFacade as SystemBackupProvider
                facadeWithBackup.unregisterSystemBackupOnShield(stateContext.lastOperationJobContext.deprovisionRequest.serviceInstanceGuid)
                return new StateChangeActionResult(go2NextState: true)
            } catch (ClassCastException cce) {
                log.error("Cast to SystemBackupOnShield for ${stateContext.kubernetesFacade.class} failed")
            }
        }
    }),

    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp())

    public static final Map<String, ServiceStateWithAction> map = new TreeMap<>()

    static {
        for (KubernetesServiceDeprovisionState serviceState : KubernetesServiceDeprovisionState.values()) {
            map.put(serviceState.getServiceInternalState(), serviceState)
        }
    }

    private final LastOperation.Status status
    private final OnStateChange<KubernetesServiceStateMachineContext> onStateChange

    KubernetesServiceDeprovisionState(LastOperation.Status lastOperationStatus, OnStateChange<KubernetesServiceStateMachineContext> onStateChange) {
        this.status = lastOperationStatus
        this.onStateChange = onStateChange
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }

    static ServiceStateWithAction of(String state) {
        return map.get(state)
    }

    @Override
    StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }

}
