package com.swisscom.cf.broker.functional

import com.swisscom.cf.servicebroker.client.ServiceBrokerClient
import org.springframework.http.HttpStatus

class AuthenticationFunctionalSpec extends BaseFunctionalSpec {

    def "catalog controller returns 401 when no credentials provided"() {
        when:
        def response = new ServiceBrokerClient(appBaseUrl,null,null).getCatalog()

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "catalog controller returns 401 when wrong credentials provided"() {
        when:
        def response = new ServiceBrokerClient(appBaseUrl,'SomeUsername','WrongPassword').getCatalog()

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "catalog controller returns 200 when correct credentials provided"() {
        when:
        def response = serviceBrokerClient.getCatalog()

        then:
        response.statusCode == HttpStatus.OK
    }
}