package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.KubernetesRedisConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.KubernetesFacade
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.KubernetesTemplateConstants
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.decorator.KubernetesTemplateVariablesDecorator
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.generators.KubernetesTemplatePasswordGenerator
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
@Log4j
@CompileStatic
class KubernetesFacadeRedis implements KubernetesFacade {

    private final KubernetesClient<?> kubernetesClient
    private final KubernetesTemplateManager kubernetesTemplateManager
    private final EndpointMapperParamsDecorated endpointMapperParamsDecorated
    private final KubernetesRedisConfig kubernetesConfig


    @Autowired
    KubernetesFacadeRedis(KubernetesRedisConfig kubernetesConfig, KubernetesClient kubernetesClient, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated) {
        this.kubernetesClient = kubernetesClient
        this.kubernetesTemplateManager = kubernetesTemplateManager
        this.endpointMapperParamsDecorated = endpointMapperParamsDecorated
        this.kubernetesConfig = kubernetesConfig
    }


    Collection<ServiceDetail> provision(ProvisionRequest context) {
        Map<String, String> passMap = (new KubernetesTemplatePasswordGenerator()).generatePassword()
        for (KubernetesTemplate kubernetesTemplate : kubernetesTemplateManager.getTemplates()) {
            (new KubernetesTemplateVariablesDecorator()).replaceTemplate(kubernetesTemplate, context, passMap, kubernetesConfig.redisConfigurationDefaults, kubernetesConfig.redisPlanDefaults)
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(kubernetesTemplate.getKind(), (new KubernetesRedisConfigUrlParams()).getParameters(context))
            kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, kubernetesTemplate.build(), urlReturn.getSecond().class)
        }
        return new LinkedList() {
            {
                add(ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_HOST, kubernetesConfig.getKubernetesRedisHost()))
                add(ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PORT, "1111")) //TO DO read from the K8S API
                add(ServiceDetail.from(ServiceDetailKey.KUBERNETES_REDIS_PASSWORD, passMap.get(KubernetesTemplateConstants.REDIS_PASS.getValue())))
            }
        }
    }


    DeprovisionResponse deprovision(DeprovisionRequest request) {
        //TODO probably just removing the whole workspace from K8s?? we need to talk to k8s Guys
        return null
    }


}
