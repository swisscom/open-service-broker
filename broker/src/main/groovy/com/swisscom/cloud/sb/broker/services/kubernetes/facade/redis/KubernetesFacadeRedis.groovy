package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.backup.SystemBackupProvider
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.Port
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.AbstractKubernetesFacade
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class KubernetesFacadeRedis extends AbstractKubernetesFacade<KubernetesRedisConfig> implements SystemBackupProvider {

    @Autowired
    KubernetesFacadeRedis(KubernetesClient kubernetesClient, KubernetesConfig kubernetesConfig, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated, KubernetesRedisConfig kubernetesRedisConfig) {
        super(kubernetesClient, kubernetesConfig, kubernetesTemplateManager, endpointMapperParamsDecorated, kubernetesRedisConfig)
    }

    @Override
    protected Map<String, String> getBindingMap(ProvisionRequest request) {
        def serviceDetailBindings = getServiceDetailBindingMap(request)
        def planBindings = getPlanParameterBindingMap(request.plan)
        def redisPassword = new StringGenerator().randomAlphaNumeric(30)
        def slaveofCommand = new StringGenerator().randomAlphaNumeric(30)
        def configCommand = new StringGenerator().randomAlphaNumeric(30)
        def otherBindings = [
                (KubernetesRedisTemplateConstants.REDIS_PASS.getValue())     : redisPassword,
                (KubernetesRedisTemplateConstants.SLAVEOF_COMMAND.getValue()): slaveofCommand,
                (KubernetesRedisTemplateConstants.CONFIG_COMMAND.getValue()) : configCommand
        ]
        // Make copy of redisConfigurationDefaults Map for thread safety
        (new HashMap<String,String>(kubernetesServiceConfig.redisConfigurationDefaults)) << planBindings << serviceDetailBindings << otherBindings
    }

    @Override
    protected Collection<ServiceDetail> buildServiceDetailsList(Map<String, String> bindingMap, List<ResponseEntity> responses) {
        def serviceResponses = responses.findAll { it?.getBody() instanceof ServiceResponse }.collect {
            it.getBody().asType(ServiceResponse.class)
        }

        def allPorts = serviceResponses.collect { it.spec.ports }.flatten() as List<Port>
        def masterPort = allPorts.findAll { it.name.equals("redis-master") }.collect {
            ServiceDetail.from(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PORT_MASTER, it.nodePort.toString())
        }
        def slavePorts = allPorts.findAll { it.name.startsWith("redis-slave") }.collect {
            ServiceDetail.from(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PORT_SLAVE, it.nodePort.toString())
        }
        def shieldPort = allPorts.findAll { it.name.equals("shield-ssh") }.collect {
            ServiceDetail.from(ShieldServiceDetailKey.SHIELD_AGENT_PORT, it.nodePort.toString())
        }

        def serviceDetails = [ServiceDetail.from(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_HOST, kubernetesServiceConfig.getKubernetesRedisHost()),
                              ServiceDetail.from(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PASSWORD, bindingMap.get(KubernetesRedisTemplateConstants.REDIS_PASS.getValue()))] +
                masterPort + slavePorts + shieldPort
        return serviceDetails
    }

    @Override
    ShieldTarget buildShieldTarget(ServiceInstance serviceInstance) {
        Integer portMaster = ServiceDetailsHelper.from(serviceInstance.details).getValue(ShieldServiceDetailKey.SHIELD_AGENT_PORT) as Integer
        new KubernetesRedisShieldTarget(namespace: serviceInstance.guid, port: portMaster)
    }

    @Override
    String systemBackupJobName(String jobPrefix, String serviceInstanceId) {
        "${jobPrefix}redis-${serviceInstanceId}"
    }

    @Override
    String systemBackupTargetName(String targetPrefix, String serviceInstanceId) {
        "${targetPrefix}redis-${serviceInstanceId}"
    }

    @Override
    String shieldAgentUrl(ServiceInstance serviceInstance) {
        "${kubernetesServiceConfig.getKubernetesRedisHost()}:${ServiceDetailsHelper.from(serviceInstance.details).getValue(ShieldServiceDetailKey.SHIELD_AGENT_PORT)}"
    }
}