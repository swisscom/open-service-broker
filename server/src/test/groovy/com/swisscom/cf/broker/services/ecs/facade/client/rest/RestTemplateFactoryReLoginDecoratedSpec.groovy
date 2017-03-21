package com.swisscom.cf.broker.services.ecs.facade.client.rest

import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.exception.ECSManagementAuthenticationException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestTemplateFactoryReLoginDecoratedSpec extends Specification {

    RestTemplateFactoryReLoginDecorated<String, String> restTemplate

    def setup() {
        RestTemplateFactory restTemplateFactory = Stub()
        restTemplate = new RestTemplateFactoryReLoginDecorated<String, String>(restTemplateFactory)
        RestTemplate restTemplateSpring = Stub()
        restTemplate.restTemplate = restTemplateSpring
        TokenManager tokenManager = Stub()
        tokenManager.refreshHeaders() >> tokenManager
        restTemplate.tokenManager = tokenManager
    }

    def "throws exeption for bad creds"() {
        when:
        HttpHeaders httpHeaders = new HttpHeaders()
        ResponseEntity<String> responseEntity = new ResponseEntity(httpHeaders, HttpStatus.FORBIDDEN)
        restTemplate.restTemplate.exchange(_, _, _, _, _) >> responseEntity
        restTemplate.exchange("http://localhost", HttpMethod.POST, "", String.class)
        then:
        thrown ECSManagementAuthenticationException
    }

    def "return proper status code for successful query"() {
        when:
        HttpHeaders httpHeaders = new HttpHeaders()
        ResponseEntity<String> responseEntity = new ResponseEntity(httpHeaders, HttpStatus.ACCEPTED)
        restTemplate.restTemplate.exchange(_, _, _, _, _) >> responseEntity
        then:
        restTemplate.exchange("http://localhost", HttpMethod.POST, "", String.class).getStatusCode() == HttpStatus.ACCEPTED
    }

    def "return proper date for successful query"() {
        when:
        HttpHeaders httpHeaders = new HttpHeaders()
        ResponseEntity<String> responseEntity = new ResponseEntity<String>("Body", httpHeaders, HttpStatus.ACCEPTED)
        restTemplate.restTemplate.exchange(_, _, _, _, _) >> responseEntity
        then:
        restTemplate.exchange("http://localhost", HttpMethod.POST, "", String.class).getBody() == "Body"
    }

}
