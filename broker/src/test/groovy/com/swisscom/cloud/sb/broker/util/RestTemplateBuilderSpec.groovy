package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.util.http.SimpleHttpServer
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification

class RestTemplateBuilderSpec extends Specification {
    private static final int port = 35000
    @Shared
    SimpleHttpServer httpServer
    @Shared
    RestTemplateBuilder restTemplateBuilder

    def setupSpec() {
        restTemplateBuilder = new RestTemplateBuilder()
    }

    def cleanupSpec() {
        httpServer?.stop()
    }

    def "build restTemplate with no features enabled"() {
        given:
        httpServer = SimpleHttpServer.create(port).buildAndStart()
        when:
        def response = makeGetRequest(restTemplateBuilder.build())
        then:
        response.statusCode == HttpStatus.OK
    }

    def "build restTemplate with basic auth"() {
        given:
        String username = 'aUsername'
        String password = 'aPassword'
        httpServer = SimpleHttpServer.create(port).withSimpleHttpAuthentication(username, password).buildAndStart()
        when:
        def response = makeGetRequest(restTemplateBuilder.withBasicAuthentication(username, password).build())
        then:
        response.statusCode == HttpStatus.OK
    }

    private def makeGetRequest(RestTemplate template) {
        return template.exchange("http://localhost:${port}", HttpMethod.GET, new HttpEntity(), String.class)
    }
}
