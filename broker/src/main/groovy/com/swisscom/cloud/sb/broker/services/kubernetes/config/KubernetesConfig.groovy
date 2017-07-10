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
    String kubernetesClientPFXPath = "/Users/taalyko2/projects/kubernetes-VPN/certificate.pfx"
    String kubernetesClientPFXPasswordPath = ""
    String kubernetesRedisV1TemplatesPath = "/Users/taalyko2/projects/new_service_broker2/open-service-broker/broker/src/test/resources/kubernetes/redis/v1/"
    int retryIntervalInSeconds = 1
    int maxRetryDurationInMinutes = 1

    HashMap<String, String> redisConfigurationDefaults = new HashMap() {
        {
            put("VERSION", "0.0.1")
            put("ENVIRONMENT", "sc1-lab")
            put("INFLUXDB_HOST", "default")
            put("INFLUXDB_PORT", "9086")
            put("INFLUXDB_USER", "default")
            put("INFLUXDB_PASS", "PASS")
            put("REDIS_PASS", "default")
            put("NODE_PORT_REDIS_MASTER", "52596")
            put("NODE_PORT_REDIS_SLAVE0", "51299")
            put("NODE_PORT_REDIS_SLAVE1", "52391")
        }
    }

    HashMap<String, String> redisPlanDefaults = new HashMap() {
        {
            put("PLAN_ID", 'redis.small')
            put("TELEGRAF_IMAGE", 'telegraf_image')
            put("MAX_CONNECTIONS", '1000')
            put("MAX_DATABASES", '10')
            put("REDIS_SERVER_MAX_MEMORY", '24')
            put("REDIS_MAX_MEMORY", '32')
            put("REDIS_MAX_CPU", '50')
            put("QUORUM", '2')
            put("REDIS_IMAGE", 'redis_image')
            put("SLAVEOF_COMMAND", 'my_SLAVEOF')
            put("CONFIG_COMMAND", 'my_CONFIG')
            put("REDIS_VERSION", '3.2.8')
            put("SENTINEL_MAX_CPU", '20')
            put("SENTINEL_MAX_MEMORY", '24')
        }
    }


}
