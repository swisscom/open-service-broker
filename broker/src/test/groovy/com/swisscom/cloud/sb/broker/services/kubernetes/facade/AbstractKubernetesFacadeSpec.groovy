package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
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
        kubernetesFacade = new AbstractKubernetesFacade(kubernetesClient, kubernetesConfig, kubernetesServiceConfig) {
            @Override
            Collection<ServiceDetail> provision(ProvisionRequest context) {
                return null
            }

            @Override
            void deprovision(DeprovisionRequest request) {

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

    private String mockReadyPodListResponse() {
        new File(this.getClass().getResource('/kubernetes/kubernetes-podlist-response-ready.json').getFile()).text
    }

    private String mockNotReadyPodListResponse() {
        new File(this.getClass().getResource('/kubernetes/kubernetes-podlist-response-notready.json').getFile()).text
    }
}
