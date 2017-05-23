package com.swisscom.cf.broker.functional

import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class SwaggerFunctionalSpec extends BaseFunctionalSpec {

    def "swagger endpoints return Http.OK"() {
        expect:
        new RestTemplate().getForEntity(appBaseUrl + '/swagger-ui.html', String.class).statusCode == HttpStatus.OK
        new RestTemplate().getForEntity(appBaseUrl + '/swagger-resources', String.class).statusCode == HttpStatus.OK
        new RestTemplate().getForEntity(appBaseUrl + '/v2/api-docs', String.class).statusCode == HttpStatus.OK
    }
}