package com.swisscom.cloud.sb.broker.servicedefinition

import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker")
class ServiceDefinitionConfig {

    List<ServiceDto> serviceDefinitions

    @Override
    public String toString() {
        return "ServiceDefinitionConfig{" +
                "serviceDefinitions=" + serviceDefinitions +
                '}'
    }
}