package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions.ECSAuthenticationProblemException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.junit.Assert
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class TokenManagerSpec extends Specification {

    TokenManager login
    ECSConfig ecsConfig
    RestTemplateFactory restTemplateFactory

    def setup() {
        ecsConfig = Stub()
        restTemplateFactory = Stub()
        login = new TokenManager(ecsConfig, restTemplateFactory)
    }

    def "getHeaders returns proper headers"() {
        given:
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.put("X-SDS-AUTH-TOKEN", new LinkedList<String>() {
            {
                add("STARTUP_DEFAULT_TOKEN")
            }
        })
        expect:
        Assert.assertArrayEquals(login.getHeaders().values().toArray(), httpHeaders.values().toArray())
        Assert.assertArrayEquals(login.getHeaders().keySet().toArray(), httpHeaders.keySet().toArray())
    }

    def "refreshHeaders renew the token"() {
        given:
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.put("X-SDS-AUTH-TOKEN", new LinkedList<String>() {
            {
                add("refreshed")
            }
        })
        ResponseEntity<String> responseEntity = new ResponseEntity(httpHeaders, HttpStatus.ACCEPTED)
        RestTemplate restTemplate = Stub()
        restTemplate.exchange(_, _, _, _, _) >> responseEntity
        login.restTemplateFactory.buildWithBasicAuthentication(_, _) >> restTemplate
        login.refreshAuthToken()
        expect:
        Assert.assertArrayEquals(login.getHeaders().values().toArray(), httpHeaders.values().toArray())
        Assert.assertArrayEquals(login.getHeaders().keySet().toArray(), httpHeaders.keySet().toArray())
    }

    def "refreshHeaders throws exception for failure"() {
        when:
        HttpHeaders httpHeaders = new HttpHeaders()
        ResponseEntity<String> responseEntity = new ResponseEntity(httpHeaders, HttpStatus.ACCEPTED)
        RestTemplate restTemplate = Stub()
        restTemplate.exchange(_, _, _, _, _) >> { throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED) }
        login.restTemplateFactory.buildWithBasicAuthentication(_, _) >> restTemplate
        login.refreshAuthToken()
        then:
        thrown ECSAuthenticationProblemException
    }


}
