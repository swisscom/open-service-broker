package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.config.KubernetesRedisConfigUrlParams
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.dto.KubernetesClientRedisDecoratedResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
@Log4j
@CompileStatic
class KubernetesRedisClientRedisDecorated {

    private final KubernetesClient<?> kubernetesClient
    private final KubernetesTemplateManager kubernetesTemplateManager
    private final EndpointMapperParamsDecorated endpointMapperParamsDecorated
    private final KubernetesConfig kubernetesConfig


    @Autowired
    KubernetesRedisClientRedisDecorated(KubernetesConfig kubernetesConfig, KubernetesClient kubernetesClient, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated) {
        this.kubernetesClient = kubernetesClient
        this.kubernetesTemplateManager = kubernetesTemplateManager
        this.endpointMapperParamsDecorated = endpointMapperParamsDecorated
        this.kubernetesConfig = kubernetesConfig
    }


    KubernetesClientRedisDecoratedResponse provision(ProvisionRequest context) {
        for (KubernetesTemplate kubernetesTemplate : kubernetesTemplateManager.getTemplates()) {
            replaceTemplate(kubernetesTemplate, context)
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(kubernetesTemplate.getKind(), (new KubernetesRedisConfigUrlParams()).getParameters(context))
            kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, kubernetesTemplate.build(), urlReturn.getSecond().class)
        }
        //TODO return "real" response object from K8s Master
        return new KubernetesClientRedisDecoratedResponse(user: "User", password: "Password")
    }


    DeprovisionResponse deprovision(DeprovisionRequest request) {
        //TODO probably just removing the whole workspace from K8s?? we need to talk to k8s Guys
        return null
    }

    private void replaceTemplate(KubernetesTemplate kubernetesTemplate, ProvisionRequest request) {
        kubernetesTemplate.replace("SERVICE_ID", request.getServiceInstanceGuid())
        kubernetesTemplate.replace("SPACE_ID", request.getSpaceGuid())
        kubernetesTemplate.replace("ORG_ID", request.getOrganizationGuid())
        replaceWithMap(kubernetesTemplate, request, kubernetesConfig.configurationParameters, kubernetesConfig.planParameters)
    }

    private void replaceWithMap(KubernetesTemplate kubernetesTemplate, ProvisionRequest request, Map<String, String>... maps) {
        for (Map<String, String> map : maps) {
            for (String key : map.keySet()) {
                kubernetesTemplate.replace(key, getPlanParameter(key, request.plan, map))
            }
        }
    }

    private String getPlanParameter(String key, Plan plan, Map<String, String> map) {
        def instanceTypeParam = plan.parameters.find { it.name == key }
        if (!instanceTypeParam) {
            return map.get(key)
        }
        return instanceTypeParam.value
    }


}
