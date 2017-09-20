package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.Port
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.Selector
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.ServiceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.Spec
import com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config.KubernetesRedisConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.dto.NamespaceResponse
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplate
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import org.springframework.data.util.Pair
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class KubernetesFacadeRedisSpec extends Specification {

    private final static String TEMPLATE_EXAMPLE = """apiVersion: v1
kind: Namespace
metadata:
  name: \"\$SERVICE_ID\"
  labels:
    service_id: \"\$SERVICE_ID\"
    service_type: redis-sentinel
    space: \"\$SPACE_ID\"
    org: \"\$ORG_ID\"
"""

    KubernetesFacadeRedis kubernetesRedisClientRedisDecorated
    KubernetesClient kubernetesClient
    KubernetesTemplateManager kubernetesTemplateManager
    ProvisionRequest provisionRequest
    DeprovisionRequest deprovisionRequest
    KubernetesConfig kubernetesConfig
    KubernetesRedisConfig kubernetesRedisConfig
    EndpointMapperParamsDecorated endpointMapperParamsDecorated

    def setup() {
        kubernetesClient = Mock()
        kubernetesConfig = Stub()
        kubernetesRedisConfig = Stub()
        deprovisionRequest = Stub()
        endpointMapperParamsDecorated = Mock()
        kubernetesRedisConfig.kubernetesRedisHost >> "host.redis"
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate(TEMPLATE_EXAMPLE)
        endpointMapperParamsDecorated.getEndpointUrlByTypeWithParams(_, _) >> new Pair("/endpoint/", new NamespaceResponse())
        kubernetesTemplateManager = Mock()
        kubernetesTemplateManager.getTemplates(_) >> new LinkedList<KubernetesTemplate>() {
            {
                add(kubernetesTemplate)
                add(kubernetesTemplate)
            }
        }
        mockProvisionRequest()
        and:
        kubernetesRedisClientRedisDecorated = new KubernetesFacadeRedis(kubernetesClient, kubernetesConfig, kubernetesRedisConfig, kubernetesTemplateManager, endpointMapperParamsDecorated)
    }


    def "provision creating a namespace with correct endpoint called"() {
        when:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        2 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, _, NamespaceResponse.class)
    }

    def "provision creating a namespace with replacing the organization"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("""org: \"\$ORG_ID\"
kind: Namespace""")
        updateTemplates(kubernetesTemplate)
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, "org: \"ORG\"\nkind: Namespace", NamespaceResponse.class)
    }

    def "provision creating a namespace with replacing the space id"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("space: \"\$SPACE_ID\"\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, "space: \"SPACE\"\nkind: Namespace", NamespaceResponse.class)
    }

    def "provision creating a namespace with replacing the Service Instance Guid id"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("name: \"\$SERVICE_ID\"\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        and:
        kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        1 * kubernetesClient.exchange('/endpoint/', HttpMethod.POST, "name: \"ID\"\nkind: Namespace", NamespaceResponse.class)
    }

    def "return correct port to the client from k8s"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("name: \"\$SERVICE_ID\"\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockServiceResponse(), HttpStatus.ACCEPTED)
        and:
        List<ServiceDetail> results = kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        "112" == ServiceDetailsHelper.from(results).getValue(ServiceDetailKey.KUBERNETES_REDIS_PORT_MASTER)
    }

    def "return correct host to the client from SB"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("name: \"\$SERVICE_ID\"\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockServiceResponse(), HttpStatus.ACCEPTED)
        and:
        List<ServiceDetail> results = kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        "host.redis" == ServiceDetailsHelper.from(results).getValue(ServiceDetailKey.KUBERNETES_REDIS_HOST)
    }

    def "returned password has proper length"() {
        when:
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate("name: \"\$SERVICE_ID\"\nkind: Namespace")
        updateTemplates(kubernetesTemplate)
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockServiceResponse(), HttpStatus.ACCEPTED)
        and:
        List<ServiceDetail> results = kubernetesRedisClientRedisDecorated.provision(provisionRequest)
        then:
        30 <= ServiceDetailsHelper.from(results).getValue(ServiceDetailKey.KUBERNETES_REDIS_PASSWORD).length()
    }

    def "deletion of service calls proper endpoint"() {
        when:
        kubernetesClient = Mock()
        deprovisionRequest.serviceInstanceGuid >> "GUID"
        and:
        kubernetesRedisClientRedisDecorated.deprovision(deprovisionRequest)
        then:
        1 * kubernetesClient.exchange('/api/v1/namespaces/GUID', HttpMethod.DELETE, "", Object.class)
    }

    private ServiceResponse mockServiceResponse() {
        ServiceResponse serviceResponse = Stub()
        Spec spec = Stub()
        Selector selector = Stub()
        selector.role >> "master"
        spec.selector >> selector
        mockPorts(spec)
        serviceResponse.spec >> spec
        serviceResponse
    }

    private void mockPorts(Spec spec) {
        Port port = Stub()
        port.name >> "redis-master"
        port.nodePort >> 112
        spec.ports >> [port]
    }

    private void updateTemplates(KubernetesTemplate kubernetesTemplate) {
        kubernetesTemplateManager.getTemplates(_) >> new LinkedList<KubernetesTemplate>() {
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
        provisionRequest.plan >> Mock(Plan)
        provisionRequest.plan.parameters >> new HashSet<Parameter>() {
            {
                add(new Parameter(name: "name", value: "value"))
            }
        }
    }
}