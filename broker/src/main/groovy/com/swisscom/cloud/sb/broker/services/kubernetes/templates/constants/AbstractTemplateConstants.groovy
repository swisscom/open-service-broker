package com.swisscom.cloud.sb.broker.services.kubernetes.templates.constants

import groovy.transform.CompileStatic

@CompileStatic
trait AbstractTemplateConstants {
    String value

    String getValue() {
        return this.value
    }
}