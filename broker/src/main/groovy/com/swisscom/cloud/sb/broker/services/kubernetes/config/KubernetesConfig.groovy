package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import com.swisscom.cloud.sb.broker.config.Config
import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.service.kubernetes')
class KubernetesConfig implements Config, EndpointConfig, AsyncServiceConfig {
    String kubernetesHost
    String kubernetesPort
    String kubernetesClientCertificate
    String kubernetesClientKey
}
