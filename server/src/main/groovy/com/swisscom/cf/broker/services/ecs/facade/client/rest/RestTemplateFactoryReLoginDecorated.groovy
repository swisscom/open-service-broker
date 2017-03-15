package com.swisscom.cf.broker.services.ecs.facade.client.rest

import com.swisscom.cf.broker.services.ecs.facade.client.commands.Login
import com.swisscom.cf.broker.services.ecs.facade.client.exception.ECSManagementAuthenticationException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate


class RestTemplateFactoryReLoginDecorated<BODY, RESPONSE> {

    private final RestTemplate restTemplate
    private final Login login

    RestTemplateFactoryReLoginDecorated(RestTemplateFactory restTemplateFactory) {
        this.restTemplate = restTemplateFactory.buildWithSSLValidationDisabled()
    }

    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      BODY body, Class<RESPONSE> responseType, Object... uriVariables) {
        ResponseEntity<RESPONSE> result = restTemplate.exchange(url, method, new HttpEntity(body, login.getHeaders()), responseType, uriVariables)
        if (isUnauthorized(result)) {
            return refreshHeadersAndExchange(url, method, body, responseType, uriVariables)
        }
        return result
    }

    private ResponseEntity<RESPONSE> refreshHeadersAndExchange(String url, HttpMethod method,
                                                               BODY body, Class<RESPONSE> responseType, Object... uriVariables) {
        ResponseEntity<RESPONSE> result = restTemplate.exchange(url, method, new HttpEntity(body, login.refreshHeaders().getHeaders()), responseType, uriVariables)
        if (isUnauthorized(result)) {
            throw new ECSManagementAuthenticationException()
        }
        return result
    }

    private boolean isUnauthorized(ResponseEntity<RESPONSE> result) {
        result.statusCode == HttpStatus.UNAUTHORIZED || result.statusCode == HttpStatus.FORBIDDEN
    }


}
