package com.swisscom.cloud.sb.broker.services.kubernetes.redis

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import org.springframework.http.HttpMethod
import spock.lang.Specification

class KubernetesRedisClientRedisDecoratedSpec extends Specification {
    KubernetesRedisClientRedisDecorated kubernetesRedisClientRedisDecorated
    KubernetesClient kubernetesClient
    KubernetesTemplateManager kubernetesTemplateManager
    ProvisionRequest provisionRequest
    KubernetesTemplate kubernetesTemplate

    def setup() {
        kubernetesClient = Mock()
        kubernetesTemplateManager = mockKubernetesTemplateManager()
        mockProvisionRequest()
        and:
        kubernetesRedisClientRedisDecorated = new KubernetesRedisClientRedisDecorated(kubernetesClient, kubernetesTemplateManager)
    }

    def "provision creating a namespace with correct templating"() {
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

    private void mockProvisionRequest() {
        provisionRequest = Mock(ProvisionRequest)
        provisionRequest.getServiceInstanceGuid() >> "ID"
        provisionRequest.getSpaceGuid() >> "GUID"
        provisionRequest.getOrganizationGuid() >> "ORG"
    }

    private KubernetesTemplateManager mockKubernetesTemplateManager() {
        kubernetesTemplateManager = Stub()
        kubernetesTemplate = Mock(KubernetesTemplate)
        kubernetesTemplate.build() >> "build"
        kubernetesTemplateManager.getNamespaceTemplate() >> kubernetesTemplate
        kubernetesTemplateManager
    }


}
