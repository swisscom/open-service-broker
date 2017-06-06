package com.swisscom.cloud.sb.broker.backup.shield

import groovy.transform.CompileStatic
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@CompileStatic
class ShieldRestClientFactory {
    ShieldRestClient build(RestTemplate restTemplate, String baseUrl, String apiKey, String agent) {
        new ShieldRestClient(restTemplate, baseUrl, apiKey, agent)
    }
}
