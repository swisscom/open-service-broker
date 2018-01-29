package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.client.ServiceBrokerClient
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class AuthenticationFunctionalSpec extends BaseFunctionalSpec {

    def "catalog controller returns 401 when no credentials provided"() {
        when:
        def response = new ServiceBrokerClient(appBaseUrl,null,null).getCatalog()

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "catalog controller returns 401 when wrong credentials provided"() {
        when:
        def response = new ServiceBrokerClient(appBaseUrl,'SomeUsername','WrongPassword').getCatalog()

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "catalog controller returns 200 when correct credentials provided"() {
        when:
        def response = serviceBrokerClient.getCatalog()

        then:
        response.statusCode == HttpStatus.OK
    }

    def "catalog controller returns Forbidden 403 when wrong role provided"() {
        when:
        def response = new ServiceBrokerClient(appBaseUrl, cfExtUser.username, cfExtUser.password).getCatalog()

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.FORBIDDEN
    }
}
