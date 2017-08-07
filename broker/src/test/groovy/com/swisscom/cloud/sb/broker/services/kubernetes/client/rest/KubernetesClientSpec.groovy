package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.security.KeyStore

class KubernetesClientSpec extends Specification {


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    KubernetesClient kubernetesClient
    KubernetesConfig kubernetesConfig
    RestTemplate restTemplate

    def setup() {
        kubernetesClient = Spy(KubernetesClient)
        kubernetesClient.enableSSLWithClientCertificate() >> null
        mockRestTemplate(kubernetesClient)
        decorateClient(kubernetesClient)
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

    private void decorateClient(KubernetesClient kubernetesClient) {
        File createdFile = folder.newFile("tmp.txt")
        kubernetesConfig = mockConfig(createdFile)
        kubernetesClient.keyStore = Mock(KeyStore)
        kubernetesClient.kubernetesConfig = kubernetesConfig
    }

    private void mockRestTemplate(KubernetesClient kubernetesClient) {
        restTemplate = Stub(RestTemplate)
        kubernetesClient.restTemplate = restTemplate
    }

    private KubernetesConfig mockConfig(File createdFile) {
        kubernetesConfig = Stub(KubernetesConfig)
        kubernetesConfig
    }


}
