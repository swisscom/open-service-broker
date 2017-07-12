package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import spock.lang.Specification

class KubernetesFacadeRedisSpec extends Specification {

    private final static String TEMPLATE_EXAMPLE = "apiVersion: v1\n" +
            "kind: Namespace\n" +
            "metadata:\n" +
            "  name: {{SERVICE_ID}}\n" +
            "  labels:\n" +
            "    service_id: {{SERVICE_ID}}\n" +
            "    service_type: redis-sentinel\n" +
            "    space: {{SPACE_ID}}\n" +
            "    org: {{ORG_ID}}"
    KubernetesFacadeRedis kubernetesRedisClientRedisDecorated
    KubernetesClient kubernetesClient
    KubernetesTemplateManager kubernetesTemplateManager
    ProvisionRequest provisionRequest
    KubernetesRedisConfig kubernetesConfig
    EndpointMapperParamsDecorated endpointMapperParamsDecorated

    def setup() {
        kubernetesClient = Mock()
        kubernetesConfig = Stub()
        endpointMapperParamsDecorated = Mock()
        kubernetesConfig.redisPlanDefaults >> Collections.emptyMap()
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate(TEMPLATE_EXAMPLE)
        endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(_, _) >> new Pair("/endpoint/", new NamespaceResponse())
        kubernetesTemplateManager = Mock()
        kubernetesTemplateManager.getTemplates() >> new LinkedList<KubernetesTemplate>() {
            {
                add(kubernetesTemplate)
                add(kubernetesTemplate)
            }
        }
        mockProvisionRequest()
        and:
        kubernetesRedisClientRedisDecorated = new KubernetesFacadeRedis(kubernetesConfig, kubernetesClient, kubernetesTemplateManager, endpointMapperParamsDecorated)
    }


    def "provision creating a namespace with correct endpoint called"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        2 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, _, NamespaceResponse.class)
    }

    def "provision creating a namespace with replacing the organization"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("org: {{ORG_ID}}\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, "org: ORG\nkind: Namespace", NamespaceResponse.class)
    }

    def "provision creating a namespace with replacing the space id"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("space: {{SPACE_ID}}\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, "space: SPACE\nkind: Namespace", NamespaceResponse.class)
    }

    def "provision creating a namespace with replacing the Service Instance Guid id"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("name: {{SERVICE_ID}}\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, "name: ID\nkind: Namespace", NamespaceResponse.class)
    }

    private void updateTemplates(KubernetesTemplate kubernetesTemplate) {
        kubernetesTemplateManager.getTemplates() >> new LinkedList<KubernetesTemplate>() {
            {
                add(kubernetesTemplate)
            }
        }
    }

    private void mockProvisionRequest() {
        provisionRequest = Mock(ProvisionRequest)
        provisionRequest.getServiceInstanceGuid() >> "ID"
        provisionRequest.getSpaceGuid() >> "SPACE"
        provisionRequest.getOrganizationGuid() >> "ORG"
    }

}
