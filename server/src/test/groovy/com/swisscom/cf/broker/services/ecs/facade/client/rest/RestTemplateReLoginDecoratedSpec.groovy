package com.swisscom.cf.broker.services.ecs.facade.client.rest

import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.exception.ECSManagementAuthenticationException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class RestTemplateReLoginDecoratedSpec extends Specification {

    RestTemplateReLoginDecorated<String, String> restTemplate

    def setup() {
        RestTemplate restTemplateSpring = Stub()
        TokenManager tokenManager = Stub()
        tokenManager.refreshAuthToken() >> tokenManager
        restTemplate = new RestTemplateReLoginDecorated<String, String>(tokenManager)
        restTemplate.restTemplate = restTemplateSpring
    }

    def "throws exeption for bad creds"() {
        when:
        restTemplate.restTemplate.exchange(_, _, _, _, _) >> {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN)
        }
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

    def "logout calls the proper endpoint"() {
        given:
        ResponseEntity<String> responseEntity = new ResponseEntity(HttpStatus.GONE)
        restTemplate.restTemplate.exchange("http/logout", HttpMethod.GET, _, String.class) >> responseEntity
        when:
        ResponseEntity<String> result = restTemplate.logout("http")
        then:
        result.getStatusCode() == HttpStatus.GONE
    }

}
