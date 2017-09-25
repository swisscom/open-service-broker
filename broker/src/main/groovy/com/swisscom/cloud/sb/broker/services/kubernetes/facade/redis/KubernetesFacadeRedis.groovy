package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.backup.SystemBackupProvider
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.Port
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.EndpointMapper
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.KubernetesRedisConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.AbstractKubernetesFacade
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.broker.util.servicedetail.ShieldServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException

@Component
@Slf4j
@CompileStatic
class KubernetesFacadeRedis extends AbstractKubernetesFacade implements SystemBackupProvider {
    private final KubernetesTemplateManager kubernetesTemplateManager
    private final EndpointMapperParamsDecorated endpointMapperParamsDecorated
    private final KubernetesRedisConfig kubernetesRedisConfig

    @Autowired
    KubernetesFacadeRedis(KubernetesClient kubernetesClient, KubernetesConfig kubernetesConfig, KubernetesRedisConfig kubernetesRedisConfig, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated) {
        super(kubernetesClient, kubernetesConfig, kubernetesRedisConfig)
        this.kubernetesTemplateManager = kubernetesTemplateManager
        this.endpointMapperParamsDecorated = endpointMapperParamsDecorated
        this.kubernetesRedisConfig = kubernetesRedisConfig
    }


    Collection<ServiceDetail> provision(ProvisionRequest context) {
        def bindingMap = createBindingMap(context)
        log.debug("Use this bindings for k8s templates: ${groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(bindingMap))}")
        def templates = kubernetesTemplateManager.getTemplates(context.plan.templateUniqueIdentifier)
        def templateEngine = new groovy.text.SimpleTemplateEngine()
        List<ResponseEntity> responses = new LinkedList()
        for (KubernetesTemplate kubernetesTemplate : templates) {
            def bindedTemplate = templateEngine.createTemplate(kubernetesTemplate.template).make(bindingMap).toString()
            log.trace("Request this template for k8s provision: ${bindedTemplate}")
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(KubernetesTemplate.getKindForTemplate(bindedTemplate), (new KubernetesRedisConfigUrlParams()).getParameters(context))
            responses.add(kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, bindedTemplate, urlReturn.getSecond().class))
        }
        return buildServiceDetailsList(bindingMap.get(KubernetesTemplateConstants.REDIS_PASS.getValue()), responses)
    }

    private Map<String, String> createBindingMap(ProvisionRequest context) {
        def serviceDetailBindings = [
                (KubernetesTemplateConstants.SERVICE_ID.getValue()): context.getServiceInstanceGuid(),
                (KubernetesTemplateConstants.SPACE_ID.getValue())  : context.getSpaceGuid(),
                (KubernetesTemplateConstants.ORG_ID.getValue())    : context.getOrganizationGuid(),
                (KubernetesTemplateConstants.PLAN_ID.getValue())   : context.plan.guid,
        ]
        Map<String, String> planBindings = context.plan.parameters.collectEntries {
            [(it.getName() as String): it.getValue() as String]
        }
        def redisPassword = new StringGenerator().randomAlphaNumeric(30)
        def slaveofCommand = new StringGenerator().randomAlphaNumeric(30)
        def configCommand = new StringGenerator().randomAlphaNumeric(30)
        def otherBindings = [
                (KubernetesTemplateConstants.REDIS_PASS.getValue())     : redisPassword,
                (KubernetesTemplateConstants.SLAVEOF_COMMAND.getValue()): slaveofCommand,
                (KubernetesTemplateConstants.CONFIG_COMMAND.getValue()) : configCommand
        ]
        kubernetesRedisConfig.redisConfigurationDefaults << planBindings << serviceDetailBindings << otherBindings
    }

    void deprovision(DeprovisionRequest request) {
        try {
            kubernetesClient.exchange(EndpointMapper.INSTANCE.getEndpointUrlByType("Namespace").getFirst() + "/" + request.serviceInstanceGuid,
                    HttpMethod.DELETE, "", Object.class)
        } catch (HttpStatusCodeException e) {
            if (e.statusCode != HttpStatus.NOT_FOUND) throw e
            else {
                log.debug("404: Tried to delete namespace " + request.serviceInstanceGuid + " which was not found. Error Message:" + e.toString())
            }
        }
    }

    private Collection<ServiceDetail> buildServiceDetailsList(String redisPassword, List<ResponseEntity> responses) {
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

        def serviceDetails = [ServiceDetail.from(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_PASSWORD, redisPassword),
                              ServiceDetail.from(KubernetesRedisServiceDetailKey.KUBERNETES_REDIS_HOST, kubernetesRedisConfig.getKubernetesRedisHost())] +
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
        "${kubernetesRedisConfig.getKubernetesRedisHost()}:${ServiceDetailsHelper.from(serviceInstance.details).getValue(ShieldServiceDetailKey.SHIELD_AGENT_PORT)}"
    }
}