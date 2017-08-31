package com.swisscom.cloud.sb.broker.util

import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestTemplateBuilderSpec extends Specification {

    def "build simple RestTemplate"() {
        given:
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
        when:
        restTemplateBuilder.build() as RestTemplate
        then:
        noExceptionThrown()
    }

    def "build RestTemplate with Basic Auth"() {
        given:
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
        when:
        def restTemplate = restTemplateBuilder.withBasicAuthentication("test", "password").build()
        then:
        restTemplate.interceptors.size() == 1
    }

    def "build RestTemplate with Diggest Auth"() {
        given:
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
        when:
        restTemplateBuilder.withDigestAuthentication("test", "password")
        then:
        def restTemplate = restTemplateBuilder.build()
        then:
        noExceptionThrown()
    }

    def "build RestTemplate with Proxy"() {
        given:
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
        when:
        restTemplateBuilder.withProxy("")
        then:
        def restTemplate = restTemplateBuilder.build()
        then:
        noExceptionThrown()
    }

    def "build RestTemplate with Basic Auth and skip SSL"() {
        given:
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
        when:
        restTemplateBuilder.withBasicAuthentication("test", "password").withSSLValidationDisabled()
        then:
        def restTemplate = restTemplateBuilder.build()
        restTemplate.interceptors.size() == 1
        noExceptionThrown()
    }
}
