package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.RolesResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceAccountsResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import org.springframework.http.HttpMethod
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class KubernetesRedisClientRedisDecoratedSpec extends Specification {
    KubernetesRedisClientRedisDecorated kubernetesRedisClientRedisDecorated
    KubernetesClient kubernetesClient
    KubernetesTemplateManager kubernetesTemplateManager
    ProvisionRequest provisionRequest
    KubernetesTemplate kubernetesTemplate
    KubernetesTemplate kubernetesAccountsTemplate
    KubernetesTemplate kubernetesRolesTemplate

    def setup() {
        kubernetesClient = Mock()
        kubernetesTemplateManager = mockKubernetesTemplateManager()
        mockProvisionRequest()
        and:
        kubernetesRedisClientRedisDecorated = new KubernetesRedisClientRedisDecorated(kubernetesClient, kubernetesTemplateManager)
    }

    def "provision creating a namespace with correct templates"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesTemplate.replace('ORG_ID', 'ORG')
    }

    def "provision creating a namespace with correct endpoint called"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/api/v1/namespaces', HttpMethod.POST, "build", NamespaceResponse.class)
    }

    def "provision creating a service accounts with correct templates"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesAccountsTemplate.replace('ORG_ID', 'ORG')
    }

    def "provision creating a service accounts with correct endpoint called"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/api/v1/namespaces/ID/serviceaccounts', HttpMethod.POST, "buildAccount", ServiceAccountsResponse.class)
    }

    def "provision creating a service roles with correct templates"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesRolesTemplate.replace('ORG_ID', 'ORG')
    }

    def "provision creating a service roles with correct endpoint called"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/apis/rbac.authorization.k8s.io/v1beta1/namespaces/ID/roles', HttpMethod.POST, "a", RolesResponse.class)
        1 * kubernetesClient.exchange('/apis/rbac.authorization.k8s.io/v1beta1/namespaces/ID/roles', HttpMethod.POST, "b", RolesResponse.class)
    }

    def "provision creating a service roles with correct single endpoint called"() {
        when:
        kubernetesRolesTemplate.build() >> "a"
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/apis/rbac.authorization.k8s.io/v1beta1/namespaces/ID/roles', HttpMethod.POST, "a", RolesResponse.class)
        0 * kubernetesClient.exchange('/apis/rbac.authorization.k8s.io/v1beta1/namespaces/ID/roles', HttpMethod.POST, "b", RolesResponse.class)
    }

    private void mockProvisionRequest() {
        provisionRequest = Mock(ProvisionRequest)
        provisionRequest.getServiceInstanceGuid() >> "ID"
        provisionRequest.getSpaceGuid() >> "GUID"
        provisionRequest.getOrganizationGuid() >> "ORG"
    }

    private KubernetesTemplateManager mockKubernetesTemplateManager() {
        kubernetesTemplateManager = Stub()
        kubernetesTemplate = Mock(KubernetesTemplate)
        kubernetesAccountsTemplate = Mock(KubernetesTemplate)
        kubernetesRolesTemplate = Mock(KubernetesTemplate)
        kubernetesTemplate.build() >> "build"
        kubernetesAccountsTemplate.build() >> "buildAccount"
        kubernetesRolesTemplate.build() >> "a---b"
        kubernetesTemplateManager.getNamespaceTemplate() >> kubernetesTemplate
        kubernetesTemplateManager.getServiceAccountsTemplate() >> kubernetesAccountsTemplate
        kubernetesTemplateManager.getServiceRolesTemplate() >> kubernetesRolesTemplate
        kubernetesTemplateManager
    }


}
