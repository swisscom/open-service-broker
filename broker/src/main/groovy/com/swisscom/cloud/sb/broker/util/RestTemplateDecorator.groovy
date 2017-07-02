package com.swisscom.cloud.sb.broker.util

import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.support.BasicAuthorizationInterceptor
import org.springframework.web.client.RestTemplate


class RestTemplateDecorator {
    static RestTemplate decorateWithBasicAuthentication(RestTemplate restTemplate, String username, String password) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors()
        if (interceptors == null) {
            interceptors = Collections.emptyList()
        } else {
            interceptors.removeAll { it instanceof BasicAuthorizationInterceptor }
        }
        interceptors.add(new BasicAuthorizationInterceptor(username, password))
        restTemplate.setInterceptors(interceptors)
        return restTemplate
    }
}
