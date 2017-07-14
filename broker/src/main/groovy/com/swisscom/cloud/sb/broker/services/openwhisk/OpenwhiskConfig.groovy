package com.swisscom.cloud.sb.broker.services.openwhisk

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.service.openwhisk")
class OpenwhiskConfig {
    String openwhiskUrl
    String openwhiskKey
    String openwhiskPass

    @Override
    public String toString() {
        return "OpenwhiskConfig{" +
                "openwhiskUrl= '" + openwhiskUrl + '\'' +
                ", openwhiskKey= '" + openwhiskKey + '\'' +
                ", openwhiskPass= '" + openwhiskPass + '\'' +
                "}"

    }
}
