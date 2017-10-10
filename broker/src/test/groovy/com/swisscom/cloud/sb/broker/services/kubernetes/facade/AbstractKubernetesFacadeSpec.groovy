package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException
import spock.lang.Specification

import java.nio.charset.Charset

class AbstractKubernetesFacadeSpec extends Specification {
    AbstractKubernetesFacade kubernetesFacade
    KubernetesClient kubernetesClient
    KubernetesConfig kubernetesConfig
    KubernetesTemplateManager kubernetesTemplateManager
    EndpointMapperParamsDecorated endpointMapperParamsDecorated
    AbstractKubernetesServiceConfig kubernetesServiceConfig

    def setup() {
        kubernetesClient = Mock()
        kubernetesConfig = Stub()
        kubernetesTemplateManager = Mock()
        endpointMapperParamsDecorated = Mock()
        kubernetesServiceConfig = Mock()
        kubernetesServiceConfig.enablePodLabelHealthzFilter >> true
        and:
        kubernetesFacade = new AbstractKubernetesFacade<AbstractKubernetesServiceConfig>(kubernetesClient, kubernetesConfig, kubernetesTemplateManager, endpointMapperParamsDecorated, kubernetesServiceConfig) {
            @Override
            protected Map<String, String> getBindingMap(ProvisionRequest context) {
                ["DUMMY_PLACEHOLDER": "dummy_value"]
            }

            @Override
            protected Collection<ServiceDetail> buildServiceDetailsList(Map<String, String> bindingMap, List<ResponseEntity> responses) {
                return null
            }
        }
    }

    def "return correct provision status when service ready"() {
        when:
        String serviceInstance = 'test'
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockReadyPodListResponse(), HttpStatus.OK)
        and:
        boolean deployTaskSuccessful = kubernetesFacade.isKubernetesDeploymentSuccessful(serviceInstance)
        then:
        deployTaskSuccessful == true
    }

    def "return correct provision status when service not ready"() {
        when:
        String serviceInstance = 'test'
        kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity(mockNotReadyPodListResponse(), HttpStatus.OK)
        and:
        boolean deployTaskSuccessful = kubernetesFacade.isKubernetesDeploymentSuccessful(serviceInstance)
        then:
        deployTaskSuccessful == false
    }

    def "no exception thrown when existing service got deleted"() {
        given:
        String serviceInstance = 'test'
        def deprovisionRequest = Mock(DeprovisionRequest)
        deprovisionRequest.serviceInstanceGuid >> serviceInstance
        1 * kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity("{}", HttpStatus.OK)
        when:
        kubernetesFacade.deprovision(deprovisionRequest)
        then:
        noExceptionThrown()
    }

    def "no exception thrown when a non-existing service got deleted"() {
        given:
        String serviceInstance = 'test'
        def deprovisionRequest = Mock(DeprovisionRequest)
        deprovisionRequest.serviceInstanceGuid >> serviceInstance
        1 * kubernetesClient.exchange(_, _, _, _) >> {
            throw new HttpServerErrorException(HttpStatus.NOT_FOUND, "Not found",
                    Mock(HttpHeaders), "{}".getBytes(), Mock(Charset))
        }
        when:
        kubernetesFacade.deprovision(deprovisionRequest)
        then:
        noExceptionThrown()
    }

    def "isKubernetesNamespaceDeleted returns false when namespace exists"() {
        given:
        1 * kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity("{}", HttpStatus.OK)
        when:
        def isKubernetesNamespaceDeleted = kubernetesFacade.isKubernetesNamespaceDeleted('test')
        then:
        isKubernetesNamespaceDeleted == false
    }

    def "isKubernetesNamespaceDeleted returns true when namespace doesn't exist"() {
        given:
        1 * kubernetesClient.exchange(_, _, _, _) >> new ResponseEntity("{}", HttpStatus.NOT_FOUND)
        when:
        def isKubernetesNamespaceDeleted = kubernetesFacade.isKubernetesNamespaceDeleted('test')
        then:
        isKubernetesNamespaceDeleted == true
    }

    private String mockReadyPodListResponse() {
        new File(this.getClass().getResource('/kubernetes/kubernetes-podlist-response-ready.json').getFile()).text
    }

    private String mockNotReadyPodListResponse() {
        new File(this.getClass().getResource('/kubernetes/kubernetes-podlist-response-notready.json').getFile()).text
    }
}
