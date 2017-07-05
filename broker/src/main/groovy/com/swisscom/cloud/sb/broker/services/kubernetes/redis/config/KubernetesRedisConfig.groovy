package com.swisscom.cloud.sb.broker.services.kubernetes.redis.config

import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cf.broker.service.kubernetes.redis.v1')
class KubernetesRedisConfig implements KubernetesConfig {
    String kubernetesTemplatesFolder = "/blabla"
    //TODO perhaps we dont need this class
}
