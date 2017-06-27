package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.RolesResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceAccountsResponse
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

        return new KubernetesClientRedisDecoratedResponse(user: "User", password: "Password")
    }


    DeprovisionResponse deprovision(DeprovisionRequest request) {
        return null
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
