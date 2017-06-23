package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
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
        createNamespace(context)
        createServiceAccounts(context)
        createRoles(context)
        return null
    }


    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return null
    }

    private void createNamespace(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getNamespaceTemplate()
        kubernetesTemplate.replace("SERVICE_ID", request.serviceInstanceGuid)
        kubernetesTemplate.replace("SPACE_ID", request.spaceGuid)
        kubernetesTemplate.replace("ORG_ID", request.organizationGuid)
        kubernetesClient.exchange("/api/v1/namespaces", HttpMethod.POST, kubernetesTemplate.build(), NamespaceResponse.class)
    }

    private void createServiceAccounts(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getServiceAccountsTemplate()
        kubernetesTemplate.replace("SERVICE_ID", request.serviceInstanceGuid)
        kubernetesTemplate.replace("SPACE_ID", request.spaceGuid)
        kubernetesTemplate.replace("ORG_ID", request.organizationGuid)
        kubernetesClient.exchange("/api/v1/namespaces/" + request.serviceInstanceGuid + "/serviceaccounts", HttpMethod.POST, kubernetesTemplate.build(), ServiceAccountsResponse.class)
    }

    private void createRoles(ProvisionRequest request) {
        KubernetesTemplate kubernetesTemplate = kubernetesTemplateManager.getServiceRolesTemplate()
        kubernetesTemplate.replace("SERVICE_ID", request.serviceInstanceGuid)
        kubernetesTemplate.replace("SPACE_ID", request.spaceGuid)
        kubernetesTemplate.replace("ORG_ID", request.organizationGuid)
        String[] roleElements = kubernetesTemplate.build().split("---")
        for (String role : roleElements) {
            kubernetesClient.exchange("/apis/rbac.authorization.k8s.io/v1beta1/namespaces/" + request.serviceInstanceGuid + "/roles", HttpMethod.POST, role, RolesResponse.class)
        }
    }


}
