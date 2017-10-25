package com.swisscom.cloud.sb.broker.services.kubernetes.facade.redis

import com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants.AbstractTemplateConstants
import groovy.transform.CompileStatic

@CompileStatic
enum KubernetesRedisTemplateConstants implements AbstractTemplateConstants{
    ROLE_MASTER("master"),
    ROLE_SLAVE("slave"),
    REDIS_PASS("REDIS_PASS"),
    NODE_PORT_REDIS_SLAVE0("NODE_PORT_REDIS_SLAVE0"),
    NODE_PORT_REDIS_MASTER("NODE_PORT_REDIS_MASTER"),
    NODE_PORT_REDIS_SLAVE1("NODE_PORT_REDIS_SLAVE1"),
    SLAVEOF_COMMAND("SLAVEOF_COMMAND"),
    CONFIG_COMMAND("CONFIG_COMMAND")

    private KubernetesRedisTemplateConstants(String value) {
        com_swisscom_cloud_sb_broker_services_kubernetes_templates_constants_AbstractTemplateConstants__value = value
    }

}