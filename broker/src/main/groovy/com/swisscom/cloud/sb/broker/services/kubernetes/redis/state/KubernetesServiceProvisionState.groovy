package com.swisscom.cloud.sb.broker.services.kubernetes.redis.state

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.action.NoOp
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.dto.KubernetesClientRedisDecoratedResponse
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic


@CompileStatic
enum KubernetesServiceProvisionState implements ServiceStateWithAction<KubernetesServiceProvisionStateMachineContext> {

    KUBERNETES_SERVICE_PROVISION(LastOperation.Status.IN_PROGRESS, new OnStateChange<KubernetesServiceProvisionStateMachineContext>
    () {
        @Override
        StateChangeActionResult triggerAction(KubernetesServiceProvisionStateMachineContext stateContext) {
            KubernetesClientRedisDecoratedResponse kubernetesClientRedisDecoratedResponse = stateContext.kubernetesClientRedisDecorated.provision(stateContext.lastOperationJobContext.provisionRequest)
            return new StateChangeActionResult(go2NextState: true, details: [
                    ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_USER, kubernetesClientRedisDecoratedResponse.user),
                    ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PASSWORD, kubernetesClientRedisDecoratedResponse.password)])
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
    private final OnStateChange<KubernetesServiceProvisionStateMachineContext> onStateChange

    KubernetesServiceProvisionState(LastOperation.Status lastOperationStatus, OnStateChange<KubernetesServiceProvisionStateMachineContext> onStateChange) {
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
    StateChangeActionResult triggerAction(KubernetesServiceProvisionStateMachineContext context) {
        return onStateChange.triggerAction(context)
    }

}
