package com.swisscom.cloud.sb.broker.backup.shield.restClient

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@CompileStatic
@Component
class ShieldRestTemplate extends RestTemplate {
    @Autowired
    ShieldRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        restTemplateBuilder.build().setErrorHandler(new ShieldRestResponseErrorHandler())
    }
}
