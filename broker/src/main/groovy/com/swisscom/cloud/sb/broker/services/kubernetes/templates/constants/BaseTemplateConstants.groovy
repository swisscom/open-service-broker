package com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants

import groovy.transform.CompileStatic

@CompileStatic
enum BaseTemplateConstants implements AbstractTemplateConstants{
    SERVICE_ID("SERVICE_ID"),
    SPACE_ID("SPACE_ID"),
    ORG_ID("ORG_ID"),
    PLAN_ID("PLAN_ID")

    private BaseTemplateConstants(String value) {
        com_swisscom_cloud_sb_broker_services_kubernetes_templates_constants_AbstractTemplateConstants__value = value
    }

}