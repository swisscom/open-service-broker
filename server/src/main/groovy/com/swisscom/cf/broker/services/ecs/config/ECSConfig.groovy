package com.swisscom.cf.broker.services.ecs.config

import com.swisscom.cf.broker.config.Config
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cf.broker.service.ecs')
class ECSConfig implements Config {
    String ecsManagementBaseUrl
    String ecsClientURL
    String ecsManagementUsername
    String ecsManagementPassword
    String ecsManagementNamespacePrefix
    String ecsManagementEnvironentPrefix
    String ecsDefaultDataServicesVpool


}
