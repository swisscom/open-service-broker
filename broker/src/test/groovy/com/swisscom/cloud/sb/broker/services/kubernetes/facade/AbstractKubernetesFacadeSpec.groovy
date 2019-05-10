/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.kubernetes.facade

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.RequestWithParameters
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.client.rest.KubernetesClient
import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.endpoint.parameters.EndpointMapperParamsDecorated
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
    TemplateConfig templateConfig
    EndpointMapperParamsDecorated endpointMapperParamsDecorated
    AbstractKubernetesServiceConfig kubernetesServiceConfig

    def setup() {
        kubernetesClient = Mock()
        kubernetesConfig = Stub()
        templateConfig = Mock()
        endpointMapperParamsDecorated = Mock()
        kubernetesServiceConfig = Mock()
        kubernetesServiceConfig.enablePodLabelHealthzFilter >> true
        kubernetesServiceConfig.templateConfig >> templateConfig
        and:
        kubernetesFacade = new AbstractKubernetesFacade<AbstractKubernetesServiceConfig>(kubernetesClient, kubernetesConfig, endpointMapperParamsDecorated, kubernetesServiceConfig) {
            @Override
            protected Map<String, String> getBindingMap(RequestWithParameters context) {
                ["DUMMY_PLACEHOLDER": "dummy_value"]
            }

            @Override
            protected Map<String, String> getUpdateBindingMap(RequestWithParameters context) {
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
