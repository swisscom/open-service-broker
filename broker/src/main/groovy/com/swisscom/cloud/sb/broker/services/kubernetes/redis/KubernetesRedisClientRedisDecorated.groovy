package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
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


    @Autowired
    KubernetesRedisClientRedisDecorated(KubernetesClient kubernetesClient, KubernetesTemplateManager kubernetesTemplateManager, EndpointMapperParamsDecorated endpointMapperParamsDecorated) {
        this.kubernetesClient = kubernetesClient
        this.kubernetesTemplateManager = kubernetesTemplateManager
        this.endpointMapperParamsDecorated = endpointMapperParamsDecorated
    }


    KubernetesClientRedisDecoratedResponse provision(ProvisionRequest context) {
        //TODO get rid of 3 lines below and replace them with information from context and the plan
        context.serviceInstanceGuid = "1e868763-d387-4a66-b17a-00f45b04abce"
        context.spaceGuid = "7e577e57-7e57-7e57-7e57-7e577e577e57"
        context.organizationGuid = "7e577e57-7e57-7e57-7e57-7e577e577e57"

        for (KubernetesTemplate kubernetesTemplate : kubernetesTemplateManager.getTemplates()) {
            replaceTemplate(kubernetesTemplate, context)
            Pair<String, ?> urlReturn = endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(kubernetesTemplate.getKind(), (new KubernetesRedisConfigUrlParams()).getParameters(context))
            kubernetesClient.exchange(urlReturn.getFirst(), HttpMethod.POST, kubernetesTemplate.build(), urlReturn.getSecond().class)
        }
        //TODO return "real" response object from K8s Master
        return new KubernetesClientRedisDecoratedResponse(user: "User", password: "Password")
    }


    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return null
    }

    //TODO make below nicer...
    private void replaceTemplate(KubernetesTemplate kubernetesTemplate, ProvisionRequest request) {
        kubernetesTemplate.replace("SERVICE_ID", request.getServiceInstanceGuid())
        kubernetesTemplate.replace("SPACE_ID", request.getSpaceGuid())
        kubernetesTemplate.replace("ORG_ID", request.getOrganizationGuid())

        kubernetesTemplate.replace("PLAN_ID", "redis.small")
        kubernetesTemplate.replace("VERSION", "0.0.1")
        kubernetesTemplate.replace("ENVIRONMENT", "sc1-lab")
        kubernetesTemplate.replace("TELEGRAF_IMAGE", "telegraf_image")
        kubernetesTemplate.replace("INFLUXDB_HOST", "influx_host")
        kubernetesTemplate.replace("INFLUXDB_PORT", "9086")
        kubernetesTemplate.replace("INFLUXDB_USER", "user_name")
        kubernetesTemplate.replace("INFLUXDB_PASS", "PASS")
        kubernetesTemplate.replace("REDIS_PASS", "redis_pass")

        kubernetesTemplate.replace("NODE_PORT_REDIS_MASTER", "42532")
        kubernetesTemplate.replace("NODE_PORT_REDIS_SLAVE0", "41254")
        kubernetesTemplate.replace("NODE_PORT_REDIS_SLAVE1", "42357")

        kubernetesTemplate.replace("PLAN_ID", "redis.small")
        kubernetesTemplate.replace("MAX_CONNECTIONS", "1000")
        kubernetesTemplate.replace("MAX_DATABASES", "10")
        kubernetesTemplate.replace("REDIS_SERVER_MAX_MEMORY", "24")
        kubernetesTemplate.replace("REDIS_MAX_MEMORY", "32")
        kubernetesTemplate.replace("REDIS_MAX_CPU", "50")
        kubernetesTemplate.replace("REDIS_PASS", "redis_pass")
        kubernetesTemplate.replace("QUORUM", "2")
        kubernetesTemplate.replace("SLAVEOF_COMMAND", "my_SLAVEOF")
        kubernetesTemplate.replace("CONFIG_COMMAND", "my_CONFIG")
        kubernetesTemplate.replace("REDIS_IMAGE", "redis_image")
        kubernetesTemplate.replace("REDIS_VERSION", "3.2.8")
        kubernetesTemplate.replace("SENTINEL_MAX_CPU", "20")
        kubernetesTemplate.replace("SENTINEL_MAX_MEMORY", "24")

        kubernetesTemplate.replace("PLAN_ID", "redis.small")
        kubernetesTemplate.replace("MAX_CONNECTIONS", "1000")
        kubernetesTemplate.replace("MAX_DATABASES", "10")
        kubernetesTemplate.replace("REDIS_SERVER_MAX_MEMORY", "24")
        kubernetesTemplate.replace("REDIS_MAX_MEMORY", "32")
        kubernetesTemplate.replace("REDIS_MAX_CPU", "50")
        kubernetesTemplate.replace("REDIS_PASS", "redis_pass")
        kubernetesTemplate.replace("VERSION", "0.0.1")
        kubernetesTemplate.replace("QUORUM", "2")
        kubernetesTemplate.replace("SLAVEOF_COMMAND", "my_SLAVEOF")
        kubernetesTemplate.replace("CONFIG_COMMAND", "my_CONFIG")
        kubernetesTemplate.replace("REDIS_IMAGE", "docker-registry.service.consul:5000/redis:0.0.94")
        kubernetesTemplate.replace("REDIS_VERSION", "3.2.8")
        kubernetesTemplate.replace("SENTINEL_MAX_CPU", "20")
        kubernetesTemplate.replace("SENTINEL_MAX_MEMORY", "24")

    }


}
