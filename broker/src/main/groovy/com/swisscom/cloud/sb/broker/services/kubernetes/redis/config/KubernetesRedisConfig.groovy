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



    String SERVICE_ID = "7fef9b0b-4cd1-4b10-a9fe-3d70132d5eb7"
    String SPACE_ID = "00000000-0000-0000-0000-000000001000"
    String ORG_ID = "00000000-0000-0000-0000-000000001000"


}
