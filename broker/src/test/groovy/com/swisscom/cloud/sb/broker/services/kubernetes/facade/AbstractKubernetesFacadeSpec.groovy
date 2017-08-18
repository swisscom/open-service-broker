package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.templates.KubernetesTemplateManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class AbstractKubernetesFacadeSpec extends Specification {
    AbstractKubernetesFacade kubernetesFacade
    AbstractKubernetesServiceConfig kubernetesServiceConfig
    KubernetesClient kubernetesClient
    KubernetesTemplateManager kubernetesTemplateManager
    KubernetesConfig kubernetesConfig

    def setup() {
        kubernetesClient = Mock()
        kubernetesConfig = Stub()
        kubernetesServiceConfig = Mock()
        kubernetesServiceConfig.enablePodLabelHealthzFilter >> true
        kubernetesTemplateManager = Mock()
        and:
        kubernetesFacade = new AbstractKubernetesFacade() {
            @Override
            Collection<ServiceDetail> provision(ProvisionRequest context) {
                return null
            }

            @Override
            void deprovision(DeprovisionRequest request) {

            }
        }
        kubernetesFacade.kubernetesClient = kubernetesClient
        kubernetesFacade.kubernetesConfig = kubernetesConfig
        kubernetesFacade.kubernetesServiceConfig = kubernetesServiceConfig
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


    private String mockReadyPodListResponse() {
        return new File('src/test/resources/kubernetes/kubernetes-podlist-response-ready.json').text
    }

    private String mockNotReadyPodListResponse() {
        return new File('src/test/resources/kubernetes/kubernetes-podlist-response-notready.json').text
    }

}
