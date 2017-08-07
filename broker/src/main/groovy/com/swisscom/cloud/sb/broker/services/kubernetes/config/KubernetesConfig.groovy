package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import com.swisscom.cloud.sb.broker.config.Config
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.service.kubernetes')
class KubernetesConfig implements Config, EndpointConfig {
    String kubernetesHost
    String kubernetesPort
    String kubernetesClientCertificate
    String kubernetesClientKey
    String kubernetesClientKeyStorePassword
    String kubernetesClientCertAlias
    String kubernetesClientKeyAlias
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes

}
