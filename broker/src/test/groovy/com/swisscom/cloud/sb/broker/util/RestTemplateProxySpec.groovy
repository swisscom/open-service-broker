package com.swisscom.cloud.sb.broker.util

import org.apache.http.auth.AuthScope
import spock.lang.Specification

class RestTemplateProxySpec extends Specification {
    def "when proxy is requested, RestTemplate should have it configured"() {
        given:
        String proxyHost = 'localhost'
        String proxyPort = '3128'
        String proxyProtocol = 'http'

        when:
        def request = new RestTemplateBuilder().withProxy(proxyHost, proxyPort, proxyProtocol)

        then:
        request.httpClientBuilder.proxy.hostname == proxyHost
        request.httpClientBuilder.proxy.port == new Integer(proxyPort)
        request.httpClientBuilder.proxy.schemeName == proxyProtocol
    }

    def "when authenticated proxy is requested, RestTemplate should have it configured"() {
        given:
        String proxyHost = 'localhost'
        String proxyPort = '3128'
        String proxyProtocol = 'http'
        String proxyUser = 'aUsername'
        String proxyPassword = 'aPassword'

        when:
        def request = new RestTemplateBuilder().withAuthenticatedProxy(proxyHost, proxyPort, proxyProtocol, proxyUser, proxyPassword)

        then:
        request.httpClientBuilder.proxy.hostname == proxyHost
        request.httpClientBuilder.proxy.port == new Integer(proxyPort)
        request.httpClientBuilder.proxy.schemeName == proxyProtocol

        def credentials = request.httpClientBuilder.credentialsProvider.getCredentials(new AuthScope(proxyHost, new Integer(proxyPort)))
        credentials.userPrincipal.getName() == proxyUser
        credentials.password == proxyPassword
    }
}
