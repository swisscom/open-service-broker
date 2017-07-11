package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachine
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.RedisBindResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.state.KubernetesServiceProvisionState
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.state.KubernetesServiceProvisionStateMachineContext
import com.swisscom.cloud.sb.broker.services.kubernetes.service.KubernetesBasedServiceProvider
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Log4j
@CompileStatic
class KubernetesRedisServiceProvider extends KubernetesBasedServiceProvider<KubernetesConfig> {

    KubernetesRedisClientRedisDecorated kubernetesClientRedisDecorated


    @Autowired
    KubernetesRedisServiceProvider(KubernetesRedisClientRedisDecorated kubernetesClientRedisDecorated) {
        this.kubernetesClientRedisDecorated = kubernetesClientRedisDecorated
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        StateMachine stateMachine = createProvisionStateMachine()
        ServiceStateWithAction currentState = getProvisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
        return AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details)
    }

    @VisibleForTesting
    private ServiceStateWithAction getProvisionState(LastOperationJobContext context) {
        ServiceStateWithAction provisionState
        if (!context.lastOperation.internalState) {
            provisionState = KubernetesServiceProvisionState.KUBERNETES_SERVICE_PROVISION
        } else {
            provisionState = KubernetesServiceProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    @VisibleForTesting
    private KubernetesServiceProvisionStateMachineContext createStateMachineContext(LastOperationJobContext context) {
        return new KubernetesServiceProvisionStateMachineContext(kubernetesClientRedisDecorated: kubernetesClientRedisDecorated,
                lastOperationJobContext: context)
    }

    @VisibleForTesting
    private StateMachine createProvisionStateMachine() {
        StateMachine stateMachine = new StateMachine([KubernetesServiceProvisionState.KUBERNETES_SERVICE_PROVISION])
        stateMachine.addAll([KubernetesServiceProvisionState.KUBERNETES_SERVICE_PROVISION_SUCCESS])
        return stateMachine
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        return null
    }

    @Override
    BindResponse bind(BindRequest request) {
        RedisBindResponseDto credentials = new RedisBindResponseDto(
                host: ServiceDetailsHelper.from(request.serviceInstance).getValue(ServiceDetailKey.KUBERNETES_REDIS_USER).toString(),
                //port: ServiceDetailsHelper.from(request.serviceInstance).getValue(ServiceDetailKey.KUBERNETES_REDIS_PORT) as int,
                password: ServiceDetailsHelper.from(request.serviceInstance).getValue(ServiceDetailKey.KUBERNETES_REDIS_PASSWORD).toString())
        return new BindResponse(credentials: credentials)
    }

    @Override
    void unbind(UnbindRequest request) {

    }
}
