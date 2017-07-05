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
@ConfigurationProperties(prefix = 'com.swisscom.cf.broker.service.kubernetes.redis.v1')
class KubernetesConfig implements Config, EndpointConfig {
    String kubernetesHost = "kubernetes-testing-service-api.service.consul"
    String kubernetesPort = "6443"
    String kubernetesClientPFXPath = "/Users/xxx/projects/kubernetes-VPN/certificate.pfx"
    String kubernetesClientPFXPasswordPath = ""
    String kubernetesRedisV1TemplatesPath = "/Users/xxx/projects/new_service_broker2/open-service-broker/broker/src/test/resources/kubernetes/redis/v1/"
    int retryIntervalInSeconds = 1
    int maxRetryDurationInMinutes = 1

}
