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
enum KubernetesServiceProvisionState implements ServiceStateWithAction<KubernetesServiceStateMachineContext> {

    KUBERNETES_SERVICE_PROVISION(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: true,
                    details: stateContext.kubernetesFacade.provision(stateContext.lastOperationJobContext.provisionRequest))
        }
    }),

    CHECK_SERVICE_DEPLOYMENT_SUCCESSFUL(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceStateMachineContext stateContext) {
            return new StateChangeActionResult(
                    go2NextState: stateContext.kubernetesFacade.isKubernetesDeploymentSuccessful(stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid))
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
                        details: facadeWithBackup.configureSystemBackup(stateContext.lastOperationJobContext.provisionRequest.serviceInstanceGuid))
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

}
