package com.swisscom.cloud.sb.broker.config

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.provisioning.parentAlias')
class ParentAliasSchedulingConfig {
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes

    @Override
    String toString() {
        return "ParentAliasSchedulingConfig{" +
                "retryIntervalInSeconds=" + retryIntervalInSeconds +
                ", maxRetryDurationInMinutes=" + maxRetryDurationInMinutes +
                "}"
    }
}
