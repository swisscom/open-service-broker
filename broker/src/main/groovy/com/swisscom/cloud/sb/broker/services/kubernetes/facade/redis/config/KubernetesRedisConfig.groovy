package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis.config

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import com.swisscom.cloud.sb.broker.config.Config
import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.service.kubernetes.redis.v1')
class KubernetesRedisConfig implements Config, EndpointConfig, AbstractKubernetesServiceConfig {
    String kubernetesRedisHost
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
    HashMap<String, String> redisConfigurationDefaults
}
