package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.test.httpserver.HttpServerApp
import com.swisscom.cloud.sb.test.httpserver.HttpServerConfig
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestTemplateBuilderTest extends Specification {
    private static final int http_port = 35000
    private static final int https_port = 35001


    def "restTemplate with no features enabled"() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port))
        when:
        def response = makeGetRequest(new RestTemplateBuilder().build())
        then:
        response.statusCode == HttpStatus.OK
        response.body.equalsIgnoreCase('hello')
        cleanup:
        httpServer?.stop()
    }

    def "restTemplate with basic auth"() {
        given:
        String username = 'aUsername'
        String password = 'aPassword'
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withSimpleHttpAuthentication(username, password))
        when:
        def response = makeGetRequest(new RestTemplateBuilder().withBasicAuthentication(username, password).build())
        then:
        response.statusCode == HttpStatus.OK
        response.body.equalsIgnoreCase('hello')
        cleanup:
        httpServer?.stop()
    }

    def "restTemplate with digest"() {
        given:
        String username = 'aUsername'
        String password = 'aPassword'
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withDigestAuthentication(username, password))
        when:
        def response = makeGetRequest(new RestTemplateBuilder().withDigestAuthentication(username, password).build())
        then:
        response.statusCode == HttpStatus.OK
        response.body.equalsIgnoreCase('hello')
        cleanup:
        httpServer?.stop()
    }

    def 'GET request over https to self signed certificate endpoint throws exception'() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withHttpsPort(https_port)
                .withKeyStore(this.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server'))
        when:
        def response = makeHttpsGetRequest(new RestTemplateBuilder().build())
        then:
        Exception ex = thrown(Exception)
        ex.cause.toString().contains('SSLHandshakeException')
        cleanup:
        httpServer?.stop()
    }

    def 'GET request over https to self signed certificate endpoint works when SSL checking is disabled'() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withHttpsPort(https_port)
                .withKeyStore(this.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server'))
        when:
        def response = makeHttpsGetRequest(new RestTemplateBuilder().withSSLValidationDisabled().build())
        then:
        response.statusCode == HttpStatus.OK
        response.body.equalsIgnoreCase('hello')
        cleanup:
        httpServer?.stop()
    }

    def 'GET request over https with a server that expects a client side certificate in pcks #12'() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withHttpsPort(https_port)
                .withKeyStore(this.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server')
                .withTrustStore(this.getClass().getResource('/server-truststore.jks').file,
                'secret'))
        when:
        def response = makeHttpsGetRequest(new RestTemplateBuilder().withSSLValidationDisabled().withClientSideCertificate(new File(this.getClass().getResource('/client.crt').file).text,
                new File(this.getClass().getResource('/client.key').file).text).build())

        then:
        response.statusCode == HttpStatus.OK
        response.body.equalsIgnoreCase('hello')
        cleanup:
        httpServer?.stop()
    }

    def 'GET request over https with a server that expects a client side certificate in pcks #12 should fail when certificate don\'t match'() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withHttpsPort(https_port)
                .withKeyStore(this.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server')
                .withTrustStore(this.getClass().getResource('/anotherkeystore').file, '123456'))

        when:
        def response = makeHttpsGetRequest(new RestTemplateBuilder().withSSLValidationDisabled().withClientSideCertificate(new File(this.getClass().getResource('/client.crt').file).text,
                new File(this.getClass().getResource('/client.key').file).text).build())

        then:
        Exception ex = thrown(Exception)
        ex
        cleanup:
        httpServer?.stop()
    }

    def 'GET request over https to self signed certificate endpoint fails when SSL checking is disabled but hostname is wrong'() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withHttpsPort(https_port)
                .withKeyStore(this.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server'))
        when:
        def response = makeHttpsGetRequestTo(new RestTemplateBuilder().withSSLValidationDisabled().build(), "127.0.0.1")
        then:
        Exception ex = thrown(Exception)
        ex
        cleanup:
        httpServer?.stop()
    }

    def 'GET request over https to self signed certificate endpoint with wrong hostname works when SSL and Hostname checking is disabled'() {
        given:
        HttpServerApp httpServer = new HttpServerApp().startServer(HttpServerConfig.create(http_port).withHttpsPort(https_port)
                .withKeyStore(this.getClass().getResource('/server-keystore.jks').file, 'secret', 'secure-server'))
        when:
        def response = makeHttpsGetRequestTo(new RestTemplateBuilder().withSSLValidationDisabled().withHostNameVerificationDisabled().build(), "127.0.0.1")
        then:
        response.statusCode == HttpStatus.OK
        response.body.equalsIgnoreCase('hello')
        cleanup:
        httpServer?.stop()
    }

    private def makeGetRequest(RestTemplate template) {
        return template.exchange("http://localhost:${http_port}", HttpMethod.GET, new HttpEntity(), String.class)
    }


    private def makeHttpsGetRequest(RestTemplate template) {
        return template.exchange("https://localhost:${https_port}", HttpMethod.GET, new HttpEntity(), String.class)
    }

    private def makeHttpsGetRequestTo(RestTemplate template, String hostname) {
        return template.exchange("https://${hostname}:${https_port}", HttpMethod.GET, new HttpEntity(), String.class)
    }
}