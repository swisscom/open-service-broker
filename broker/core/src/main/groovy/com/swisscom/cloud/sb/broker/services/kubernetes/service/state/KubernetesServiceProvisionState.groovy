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
import com.swisscom.cloud.sb.broker.model.RequestWithParameters
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
enum KubernetesServiceProvisionState implements ServiceStateWithAction<KubernetesServiceStateMachineContext> {

    KUBERNETES_SERVICE_PROVISION(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: true,
                    details: stateContext.kubernetesFacade.provision(getRequest(stateContext)))
        }
    }),

    KUBERNETES_SERVICE_UPDATE(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: true,
                    details: stateContext.kubernetesFacade.update(getRequest(stateContext)))
        }
    }),

    KUBERNETES_SERVICE_REMOVE_AFFINITY(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: true,
                    details: stateContext.kubernetesFacade.removeAffinity(getRequest(stateContext)))
        }
    }),

    CHECK_SERVICE_REMOVE_AFFINITY_SUCCESSFUL(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: stateContext.kubernetesFacade.isKubernetesUpdateSuccessful(getRequest(stateContext).serviceInstanceGuid))
        }
    }),

    CHECK_SERVICE_DEPLOYMENT_REMOVE_AFFINITY_SUCCESSFUL(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: stateContext.kubernetesFacade.isKubernetesDeploymentSuccessful(getRequest(stateContext).serviceInstanceGuid))
        }
    }),

    CHECK_SERVICE_UPDATE_SUCCESSFUL(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: stateContext.kubernetesFacade.isKubernetesUpdateSuccessful(getRequest(stateContext).serviceInstanceGuid))
        }
    }),

    CHECK_SERVICE_DEPLOYMENT_SUCCESSFUL(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: stateContext.kubernetesFacade.isKubernetesDeploymentSuccessful(getRequest(stateContext).serviceInstanceGuid))
        }
    }),

    REGISTER_SHIELD_BACKUP(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            try {
                def facadeWithBackup = stateContext.kubernetesFacade as SystemBackupProvider
                return new StateChangeActionResult(
                        go2NextState: true,
                        details: facadeWithBackup.configureSystemBackup(getRequest(stateContext).serviceInstanceGuid))
            } catch (ClassCastException cce) {
                log.error("Cast to SystemBackupOnShield for ${stateContext.kubernetesFacade.class} failed")
            }
        }
    }),

    KUBERNETES_SERVICE_PROVISION_SUCCESS(LastOperation.Status.SUCCESS, new NoOp()),

    KUBERNETES_SERVICE_PROVISION_FAILED(LastOperation.Status.FAILED, new NoOp())

    public static final Map<String, ServiceStateWithAction> map = new TreeMap<>()

    static {
        for (KubernetesServiceProvisionState serviceState : KubernetesServiceProvisionState.values()) {
            map.put(serviceState.getServiceInternalState(), serviceState)
        }
    }

    private final LastOperation.Status status
    private final OnStateChange<KubernetesServiceStateMachineContext> onStateChange

    KubernetesServiceProvisionState(LastOperation.Status lastOperationStatus, OnStateChange<KubernetesServiceStateMachineContext> onStateChange) {
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

    static RequestWithParameters getRequest(KubernetesServiceStateMachineContext stateContext){
        stateContext.lastOperationJobContext.provisionRequest?.serviceInstanceGuid ? stateContext.lastOperationJobContext.provisionRequest : stateContext.lastOperationJobContext.updateRequest
    }

}
