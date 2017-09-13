package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.util.httpserver.HttpServerApp
import com.swisscom.cloud.sb.broker.util.httpserver.HttpServerConfig
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestTemplateBuilderSpec extends Specification {
    private static final int port = 35000


    def "restTemplate with no features enabled"() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(port))
        when:
        def response = makeGetRequest(new RestTemplateBuilder().build())
        then:
        response.statusCode == HttpStatus.OK
        cleanup:
        httpServer.stop()
    }

    def "restTemplate with basic auth"() {
        given:
        String username = 'aUsername'
        String password = 'aPassword'
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(port).withSimpleHttpAuthentication(username, password))
        when:
        def response = makeGetRequest(new RestTemplateBuilder().withBasicAuthentication(username, password).build())
        then:
        response.statusCode == HttpStatus.OK
        cleanup:
        httpServer.stop()
    }

    def "restTemplate with digest"() {
        given:
        String username = 'aUsername'
        String password = 'aPassword'
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(port).withDigestAuthentication(username, password))
        when:
        def response = makeGetRequest(new RestTemplateBuilder().withDigestAuthentication(username, password).build())
        then:
        response.statusCode == HttpStatus.OK
        cleanup:
        httpServer.stop()
    }

    private def makeGetRequest(RestTemplate template) {
        return template.exchange("http://localhost:${port}", HttpMethod.GET, new HttpEntity(), String.class)
    }
}