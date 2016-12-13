package com.swisscom.cf.broker.functional

import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class GetVersionFunctionalSpec extends BaseFunctionalSpec {

    def "version controller returns 200 and valid version"() {
        when:
        def response = new RestTemplate().getForEntity(appBaseUrl + '/version', String.class)

        then:
        response.statusCode == HttpStatus.OK
    }
}