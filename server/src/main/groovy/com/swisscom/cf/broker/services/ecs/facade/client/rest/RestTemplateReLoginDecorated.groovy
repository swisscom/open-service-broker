package com.swisscom.cf.broker.services.ecs.facade.client.rest

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.exception.ECSManagementAuthenticationException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate


class RestTemplateReLoginDecorated<BODY, RESPONSE> {

    @VisibleForTesting
    private RestTemplate restTemplate
    @VisibleForTesting
    private TokenManager tokenManager

    RestTemplateReLoginDecorated(TokenManager tokenManager) {
        this.tokenManager = tokenManager
        this.restTemplate = new RestTemplateFactory().build()
    }

    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      BODY body, Class<RESPONSE> responseType) {
        try {
            ResponseEntity<RESPONSE> result = restTemplate.exchange(url, method, new HttpEntity(body, tokenManager.getHeaders()), responseType)
            return result
        } catch (HttpClientErrorException e) {
            if (e.statusCode.equals(HttpStatus.FORBIDDEN)) {
                return refreshHeadersAndExchange(url, method, body, responseType)
            }
            throw e
        }
    }

    private ResponseEntity<RESPONSE> refreshHeadersAndExchange(String url, HttpMethod method, BODY body, Class<RESPONSE> responseType) {
        try {
            ResponseEntity<RESPONSE> result = restTemplate.exchange(url, method, new HttpEntity(body, tokenManager.refreshAuthToken().getHeaders()), responseType)
            return result
        } catch (HttpClientErrorException e ) {
            if (e.statusCode.equals(HttpStatus.FORBIDDEN)) {
                throw new ECSManagementAuthenticationException()
            }
            throw e
        }
    }
}
