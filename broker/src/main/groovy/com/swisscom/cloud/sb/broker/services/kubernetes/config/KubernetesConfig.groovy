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
trait KubernetesConfig implements Config, EndpointConfig {
    String kubernetesHost = "kubernetes-testing-service-api.service.consul"
    String kubernetesPort = "6443"
    String kubernetesClientPFXPath = "/Users/xxxx/projects/kubernetes-VPN/certificate.pfx"
    String kubernetesClientPFXPasswordPath = ""
    int retryIntervalInSeconds = 1
    int maxRetryDurationInMinutes = 1

}
