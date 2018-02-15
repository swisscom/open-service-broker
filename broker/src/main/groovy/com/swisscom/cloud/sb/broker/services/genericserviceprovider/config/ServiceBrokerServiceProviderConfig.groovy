package com.swisscom.cloud.sb.broker.services.genericserviceprovider.config

import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.service.servicebrokerserviceprovider')

class ServiceBrokerServiceProviderConfig implements AsyncServiceConfig {
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
}