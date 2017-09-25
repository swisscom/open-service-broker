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

}
