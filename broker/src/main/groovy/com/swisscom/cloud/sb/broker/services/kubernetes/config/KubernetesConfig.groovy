package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.config.Config
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@AutoClone
@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cf.broker.service.kubernetes.redis.v1')
class KubernetesConfig implements Config {
    String kubernetesHost = "kubernetes-testing-service-api.service.consul"
    String kubernetesPort = "6443"
    String kubernetesClientPFXPath = "/Users/taalyko2/projects/kubernetes-VPN/certificate.pfx"
    String kubernetesClientPFXPasswordPath = ""
    String kubernetesTemplatesFolder = "/blabla"

    String SERVICE_ID = "7fef9b0b-4cd1-4b10-a9fe-3d70132d5eb7"
    String SPACE_ID = "00000000-0000-0000-0000-000000001000"
    String ORG_ID = "00000000-0000-0000-0000-000000001000"
    String PLAN_ID = "redis.small"
    String VERSION = "0.0.1"
    //runtime params
    String NODE_PORT_REDIS = "31151"
    String NODE_PORT_SENTINEL = "31886"
    //redis params
    String MASTER_NAME = "2345mafdi34q5q345w45jk"
    String REDIS_PASS = "7fef9b0b-4cd1-4b10-a9fe-3d70132d5eb7"
    String MAX_CONNECTIONS = "1000"
    String MAX_DATABASES = "10"
    //MAX_MEMORY in MB, REDIS_SERVER = "=" MAX_MEMORY in redis - server.conf
    String REDIS_SERVER_MAX_MEMORY = "24"
    String REDIS_MAX_MEMORY = "32"
    String REDIS_MAX_CPU = "50"
    String QUORUM = "2"
    String REDIS_IMAGE = "docker-registry.service.consul:5000\\/redis:0.1.15"
    String REDIS_VERSION = "3.2.8"
    //sentinel params
    String SENTINEL_MAX_CPU = "20"
    String SENTINEL_MAX_MEMORY = "16"
    //telegraf params
    String ENVIRONMENT = "ci1-lab19ch"
    String TELEGRAF_IMAGE = "docker-registry.service.consul:5000\\/redis-telegraf-sidecar:latest"
    String INFLUXDB_HOST = "inbound.hydra-177.appcloud.swisscom.com"
    String INFLUXDB_PORT = "9086"
    String INFLUXDB_USER = "admin"
    String INFLUXDB_PASS = "ZXl049aiM9lIeItKbanmmS5F"

}
