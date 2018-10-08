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

package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class KubernetesClientSpec extends Specification {
    KubernetesClient kubernetesClient
    KubernetesConfig kubernetesConfig
    RestTemplateBuilder restTemplateBuilder
    RestTemplate restTemplate

    def setup() {
        restTemplate = Stub(RestTemplate)
        restTemplateBuilder = Mock(RestTemplateBuilder)
        restTemplateBuilder.build() >> restTemplate
        restTemplateBuilder.withClientSideCertificate(_, _) >> restTemplateBuilder
        restTemplateBuilder.withSSLValidationDisabled() >> restTemplateBuilder
        kubernetesConfig = Stub(kubernetesClientCertificate: "", kubernetesClientKey: "")
        kubernetesClient = new KubernetesClient(kubernetesConfig, restTemplateBuilder)
    }

    def "exchange uses the right endpoint"() {
        given:
        restTemplate.exchange("https://:/endpoint", _, _, _) >> new ResponseEntity("OK", HttpStatus.ACCEPTED)
        expect:
        kubernetesClient.exchange("endpoint", HttpMethod.GET, "body", String.class) != null
    }

    def "exchange returns correct result"() {
        given:
        restTemplate.exchange(_, _, _, _) >> new ResponseEntity("OK", HttpStatus.ACCEPTED)
        and:
        ResponseEntity result = kubernetesClient.exchange("endpoint", HttpMethod.GET, "body", String.class)
        expect:
        result.getBody() == "OK"
    }

    def "convert null to json don't throw exception"() {
        String result = kubernetesClient.convertYamlToJson(null)
        expect:
        "" == result
    }

    def "convert empty string to json don't throw exception"() {
        String result = kubernetesClient.convertYamlToJson("")
        expect:
        "" == result
    }

    def "merge merges Maps correctly"() {
        given:
        Map onto = ['key1': 'value1',
                    'key2': 'value2']
        Map overwrite = ['key2': 'overwrite']
        when:
        def result = kubernetesClient.merge(onto, overwrite)
        then:
        result.key1 == 'value1'
        result.key2 == 'overwrite'
        result.key2 != 'value2'
    }

    def "merge merges Maps with Arrays correctly"() {
        given:
        ArrayList<Object> array1 = new ArrayList<Object>()
        array1.add(['name': 'one'])
        Map onto = ['key1' : 'value1',
                    'key2' : 'value2',
                    'items': array1]
        ArrayList<Object> array2 = new ArrayList<Object>()
        array2.add(['name': 'two'])
        Map overwrite = ['key2' : 'overwrite',
                         'items': array2]
        when:
        def result = kubernetesClient.merge(onto, overwrite)
        then:
        result.key1 == 'value1'
        result.key2 == 'overwrite'
        result.items.first()
        result.items.first().name == 'two'
    }

}
