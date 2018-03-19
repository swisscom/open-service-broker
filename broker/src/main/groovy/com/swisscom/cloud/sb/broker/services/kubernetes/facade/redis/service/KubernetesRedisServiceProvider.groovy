package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.service

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.backup.shield.ShieldBackupRestoreProvider
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.context.CloudFoundryContextRestrictedOnly
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachine
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.RedisBindResponseDto
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.KubernetesFacadeRedis
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.KubernetesRedisServiceDetailKey
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.KubernetesRedisShieldTarget
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.service.state.KubernetesServiceDeprovisionState
import com.swisscom.cloud.sb.broker.services.kubernetes.service.state.KubernetesServiceProvisionState
import com.swisscom.cloud.sb.broker.services.kubernetes.service.state.KubernetesServiceStateMachineContext
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sun.reflect.generics.reflectiveObjects.NotImplementedException

@Component
@Log4j
@CompileStatic
class KubernetesRedisServiceProvider extends AsyncServiceProvider<KubernetesRedisConfig> implements ShieldBackupRestoreProvider, CloudFoundryContextRestrictedOnly {

    KubernetesFacadeRedis kubernetesClientRedisDecorated

    @Autowired
    KubernetesRedisServiceProvider(KubernetesFacadeRedis kubernetesClientRedisDecorated) {
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
    private KubernetesServiceStateMachineContext createStateMachineContext(LastOperationJobContext context) {
        return new KubernetesServiceStateMachineContext(kubernetesFacade: kubernetesClientRedisDecorated,
                lastOperationJobContext: context)
    }

    @VisibleForTesting
    private StateMachine createProvisionStateMachine() {
        StateMachine stateMachine = new StateMachine([KubernetesServiceProvisionState.KUBERNETES_SERVICE_PROVISION,
                                                      KubernetesServiceProvisionState.CHECK_SERVICE_DEPLOYMENT_SUCCESSFUL,
                                                      KubernetesServiceProvisionState.REGISTER_SHIELD_BACKUP,
                                                      KubernetesServiceProvisionState.KUBERNETES_SERVICE_PROVISION_SUCCESS])
        return stateMachine
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        StateMachine stateMachine = createDeprovisionStateMachine()
        ServiceStateWithAction currentState = getDeprovisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
        return Optional.of(AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState, actionResult.details))
    }

    @VisibleForTesting
    private ServiceStateWithAction getDeprovisionState(LastOperationJobContext context) {
        ServiceStateWithAction deprovisionState
        if (!context.lastOperation.internalState) {
            deprovisionState = KubernetesServiceDeprovisionState.KUBERNETES_NAMESPACE_DELETION
        } else {
            deprovisionState = KubernetesServiceDeprovisionState.of(context.lastOperation.internalState)
        }
        return deprovisionState
    }

    @VisibleForTesting
    private StateMachine createDeprovisionStateMachine() {
        StateMachine stateMachine = new StateMachine([KubernetesServiceDeprovisionState.KUBERNETES_NAMESPACE_DELETION,
                                                      KubernetesServiceDeprovisionState.CHECK_NAMESPACE_DELETION_SUCCESSFUL,
                                                      KubernetesServiceDeprovisionState.UNREGISTER_SHIELD_SYSTEM_BACKUP])
        stateMachine.addAll([KubernetesServiceDeprovisionState.DEPROVISION_SUCCESS])
        return stateMachine
    }

    @Override
    BindResponse bind(BindRequest request) {
        RedisBindResponseDto credentials = new RedisBindResponseDto(
                host: ServiceDetailsHelper.from(request.serviceInstance).getValue(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_HOST).toString(),
                masterPort: ServiceDetailsHelper.from(request.serviceInstance).getValue(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PORT_MASTER) as int,
                slavePorts: ServiceDetailsHelper.from(request.serviceInstance).getDetails().findAll {
                    it.key.equals(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PORT_SLAVE.getKey())
                }.collect { it.getValue() as int } as List,
                password: ServiceDetailsHelper.from(request.serviceInstance).getValue(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PASSWORD).toString()
        )
        return new BindResponse(credentials: credentials)
    }

    @Override
    void unbind(UnbindRequest request) {

    }

    UpdateResponse update(UpdateRequest request) {
        ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.throwNew()
        return null
    }

    @Override
    ShieldTarget buildShieldTarget(ServiceInstance serviceInstance) {
        new KubernetesRedisShieldTarget(namespace: serviceInstance.guid, port: ServiceDetailsHelper.from(serviceInstance.details).getValue(ShieldServiceDetailKey.SHIELD_AGENT_PORT) as Integer)
    }

    @Override
    String shieldAgentUrl(ServiceInstance serviceInstance) {
        "${serviceConfig.getKubernetesRedisHost()}:${ServiceDetailsHelper.from(serviceInstance.details).getValue(ShieldServiceDetailKey.SHIELD_AGENT_PORT)}"
    }
}
