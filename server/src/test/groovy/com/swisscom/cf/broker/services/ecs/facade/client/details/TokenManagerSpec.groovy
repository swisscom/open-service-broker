package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions.ECSAuthenticationProblemException
import org.junit.Assert
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class TokenManagerSpec extends Specification {

    TokenManager login

    def setup() {
        login = new TokenManager()
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
        mockLogin()
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.put("X-SDS-AUTH-TOKEN", new LinkedList<String>() {
            {
                add("refreshed")
            }
        })
        ResponseEntity<String> responseEntity = new ResponseEntity(httpHeaders, HttpStatus.ACCEPTED)
        login.restTemplate.exchange(_, _, _, _, _) >> responseEntity
        login.refreshHeaders()
        expect:
        Assert.assertArrayEquals(login.getHeaders().values().toArray(), httpHeaders.values().toArray())
        Assert.assertArrayEquals(login.getHeaders().keySet().toArray(), httpHeaders.keySet().toArray())
    }

    def "refreshHeaders throws exception for failure"() {
        when:
        mockLogin()
        HttpHeaders httpHeaders = new HttpHeaders()
        ResponseEntity<String> responseEntity = new ResponseEntity(httpHeaders, HttpStatus.ACCEPTED)
        login.restTemplate.exchange(_, _, _, _, _) >> responseEntity
        login.refreshHeaders()
        then:
        thrown ECSAuthenticationProblemException
    }

    def mockLogin() {
        RestTemplate restTemplate = Stub()
        login.restTemplate = restTemplate
        ECSConfig ecsConfig = Stub()
        login.ecsConfig = ecsConfig
    }


}
