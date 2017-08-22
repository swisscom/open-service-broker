package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.EndpointMapper
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.KubernetesRedisConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.AbstractKubernetesFacade
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.StringGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml

@Component
@Slf4j
@CompileStatic
class KubernetesFacadeRedis extends AbstractKubernetesFacade {
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
        def redisPassword = new StringGenerator().randomAlphaNumeric(30)
        def bindingMap = createBindingMap(context, redisPassword)
        def templateEngine = new groovy.text.SimpleTemplateEngine()
        List<ResponseEntity> responses = new LinkedList()
        def templates = kubernetesTemplateManager.getTemplates()
        for (KubernetesTemplate kubernetesTemplate : templates) {
            def bindedTemplate = templateEngine.createTemplate(kubernetesTemplate.template).make(bindingMap).toString()
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(getKindForTemplate(bindedTemplate), (new KubernetesRedisConfigUrlParams()).getParameters(context))
            responses.add(kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, bindedTemplate, urlReturn.getSecond().class))
        }
        return buildServiceDetailsList(redisPassword, responses)
    }

    private Map<String, String> createBindingMap(ProvisionRequest context, String redisPassword) {
        def serviceDetailBindings = [
                (KubernetesTemplateConstants.SERVICE_ID.getValue()): context.getServiceInstanceGuid(),
                (KubernetesTemplateConstants.SPACE_ID.getValue())  : context.getSpaceGuid(),
                (KubernetesTemplateConstants.ORG_ID.getValue())    : context.getOrganizationGuid(),
        ]
        Map<String, String> planBindings = context.plan.parameters.collectEntries {
            [(it.getName() as String): it.getValue() as String]
        }
        def passwordAsMap = [(KubernetesTemplateConstants.REDIS_PASS.getValue()): redisPassword]
        kubernetesRedisConfig.redisConfigurationDefaults << planBindings << serviceDetailBindings <<  passwordAsMap
    }

    void deprovision(DeprovisionRequest request) {
        kubernetesClient.exchange(EndpointMapper.INSTANCE.getEndpointUrlByType("Namespace").getFirst() + "/" + request.serviceInstanceGuid,
                HttpMethod.DELETE, "", Object.class)
    }

    private String getKindForTemplate(String template) {
        return ((Map) new Yaml().load(template)).'kind' as String
    }

    private Collection<ServiceDetail> buildServiceDetailsList(String redisPassword, List<ResponseEntity> responses) {
        def redisMasters = responses.findAll { it?.getBody() instanceof ServiceResponse }.collect {
            it.getBody().asType(ServiceResponse.class)
        }.findAll {
            KubernetesTemplateConstants.ROLE_MASTER.getValue().equals(it.spec.selector.role)
        }
        def redisMasterPorts = redisMasters.collect { it.spec.ports?.first().nodePort.toString() }

        return [ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_HOST, kubernetesRedisConfig.getKubernetesRedisHost()),
                ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PORT, (!redisMasterPorts.empty) ? redisMasterPorts.first() : ""),
                ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PASSWORD, redisPassword)
        ]
    }
}