package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ConfigMapResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.DeploymentResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.RolesResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceAccountsResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.redis.dto.KubernetesClientRedisDecoratedResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
@Log4j
@CompileStatic
class KubernetesRedisClientRedisDecorated {

    private final KubernetesClient<?> kubernetesClient
    private final KubernetesTemplateManager kubernetesTemplateManager

    @Autowired
    KubernetesRedisClientRedisDecorated(KubernetesClient kubernetesClient, KubernetesTemplateManager kubernetesTemplateManager) {
        this.kubernetesClient = kubernetesClient
        this.kubernetesTemplateManager = kubernetesTemplateManager
    }


    KubernetesClientRedisDecoratedResponse provision(ProvisionRequest context) {
        context.serviceInstanceGuid = "1e868763-d387-4a66-b17a-00f45b04abcd"
        context.spaceGuid = "7e577e57-7e57-7e57-7e57-7e577e577e57"
        context.organizationGuid = "7e577e57-7e57-7e57-7e57-7e577e577e57"
        createNamespace(context)
        createServiceAccounts(context)
        createRoles(context)
        createTelegraf(context)
        createServices(context)
        createDeploymentSentinel(context)
        createDeploymentOperator(context)
        return new KubernetesClientRedisDecoratedResponse(user: "User", password: "Password")
    }


    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return null
    }

    private void createDeploymentOperator(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getDeploymentOperatorTemplate()
        replaceTemplate(kubernetesTemplate, request)
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
        System.out.println()
        System.out.println(kubernetesTemplate.build())
        System.out.println()
        kubernetesClient.exchange("/apis/apps/v1beta1/namespaces/" + request.serviceInstanceGuid + "/deployments", HttpMethod.POST, kubernetesTemplate.build(), DeploymentResponse.class)
    }

    private void createDeploymentSentinel(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getDeploymentSentinelTemplate()
        replaceTemplate(kubernetesTemplate, request)
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
        kubernetesClient.exchange("/apis/apps/v1beta1/namespaces/" + request.serviceInstanceGuid + "/deployments", HttpMethod.POST, kubernetesTemplate.build(), DeploymentResponse.class)
    }

    private void createServices(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getServicesTemplate()
        replaceTemplate(kubernetesTemplate, request)
        kubernetesTemplate.replace("NODE_PORT_REDIS_MASTER", "42532")
        kubernetesTemplate.replace("NODE_PORT_REDIS_SLAVE0", "41254")
        kubernetesTemplate.replace("NODE_PORT_REDIS_SLAVE1", "42357")

        String[] serviceElements = kubernetesTemplate.build().split("---")
        for (String service : serviceElements) {
            kubernetesClient.exchange("/api/v1/namespaces/" + request.serviceInstanceGuid + "/services", HttpMethod.POST, service, ServiceResponse.class)
        }
    }

    private void createTelegraf(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getTelegrafConfigTemplate()
        replaceTemplate(kubernetesTemplate, request)
        kubernetesTemplate.replace("PLAN_ID", "redis.small")
        kubernetesTemplate.replace("VERSION", "0.0.1")
        kubernetesTemplate.replace("ENVIRONMENT", "sc1-lab")
        kubernetesTemplate.replace("TELEGRAF_IMAGE", "telegraf_image")
        kubernetesTemplate.replace("INFLUXDB_HOST", "influx_host")
        kubernetesTemplate.replace("INFLUXDB_PORT", "9086")
        kubernetesTemplate.replace("INFLUXDB_USER", "user_name")
        kubernetesTemplate.replace("INFLUXDB_PASS", "PASS")
        kubernetesTemplate.replace("REDIS_PASS", "redis_pass")
        kubernetesClient.exchange("/api/v1/namespaces/" + request.serviceInstanceGuid + "/configmaps", HttpMethod.POST, kubernetesTemplate.build(), ConfigMapResponse.class)
    }

    private void createNamespace(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getNamespaceTemplate()
        replaceTemplate(kubernetesTemplate, request)
        kubernetesClient.exchange("/api/v1/namespaces", HttpMethod.POST, kubernetesTemplate.build(), NamespaceResponse.class)
    }

    private void createServiceAccounts(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getServiceAccountsTemplate()
        replaceTemplate(kubernetesTemplate, request)
        kubernetesClient.exchange("/api/v1/namespaces/" + request.serviceInstanceGuid + "/serviceaccounts", HttpMethod.POST, kubernetesTemplate.build(), ServiceAccountsResponse.class)
    }

    private void createRoles(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getServiceRolesTemplate()
        replaceTemplate(kubernetesTemplate, request)
        String[] roleElements = kubernetesTemplate.build().split("---")
        for (String role : roleElements) {
            kubernetesClient.exchange("/apis/rbac.authorization.k8s.io/v1beta1/namespaces/" + request.serviceInstanceGuid + "/roles", HttpMethod.POST, role, RolesResponse.class)
        }
    }


    private void replaceTemplate(KubernetesTemplate kubernetesTemplate, ProvisionRequest request) {
        kubernetesTemplate.replace("SERVICE_ID", request.getServiceInstanceGuid())
        kubernetesTemplate.replace("SPACE_ID", request.getSpaceGuid())
        kubernetesTemplate.replace("ORG_ID", request.getOrganizationGuid())
    }


}
