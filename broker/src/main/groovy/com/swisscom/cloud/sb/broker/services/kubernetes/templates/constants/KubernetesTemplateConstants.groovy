package com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants

import groovy.transform.CompileStatic

@CompileStatic
enum KubernetesTemplateConstants {
    SERVICE_ID("SERVICE_ID"),
    SPACE_ID("SPACE_ID"),
    ORG_ID("ORG_ID"),
    PLAN_ID("PLAN_ID"),
    ROLE_MASTER("master"),
    ROLE_SLAVE("slave"),
    REDIS_PASS("REDIS_PASS"),
    NODE_PORT_REDIS_SLAVE0("NODE_PORT_REDIS_SLAVE0"),
    NODE_PORT_REDIS_MASTER("NODE_PORT_REDIS_MASTER"),
    NODE_PORT_REDIS_SLAVE1("NODE_PORT_REDIS_SLAVE1"),
    SLAVEOF_COMMAND("SLAVEOF_COMMAND"),
    CONFIG_COMMAND("CONFIG_COMMAND")

    private final String value

    private KubernetesTemplateConstants(String value) {
        this.value = value
    }

    String getValue() {
        return this.value
    }
}