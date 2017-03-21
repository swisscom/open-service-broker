package com.swisscom.cf.broker.services.ecs.facade.client.rest

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.exception.ECSManagementAuthenticationException
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class RestTemplateFactoryReLoginDecorated<BODY, RESPONSE> {

    @VisibleForTesting
    private RestTemplate restTemplate
    @VisibleForTesting
    private TokenManager tokenManager

    RestTemplateFactoryReLoginDecorated(RestTemplateFactory restTemplateFactory) {
        this.restTemplate = restTemplateFactory.buildWithSSLValidationDisabled()
    }

    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      BODY body, Class<RESPONSE> responseType) {
        ResponseEntity<RESPONSE> result = restTemplate.exchange(url, method, new HttpEntity(body, tokenManager.getHeaders()), responseType)
        if (isUnauthorized(result)) {
            return refreshHeadersAndExchange(url, method, body, responseType)
        }
        return result
    }

    private ResponseEntity<RESPONSE> refreshHeadersAndExchange(String url, HttpMethod method, BODY body, Class<RESPONSE> responseType) {
        ResponseEntity<RESPONSE> result = restTemplate.exchange(url, method, new HttpEntity(body, tokenManager.refreshHeaders().getHeaders()), responseType)
        if (isUnauthorized(result)) {
            throw new ECSManagementAuthenticationException()
        }
        return result
    }

    private boolean isUnauthorized(ResponseEntity<RESPONSE> result) {
        result.statusCode == HttpStatus.FORBIDDEN
    }


}
