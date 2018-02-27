package com.swisscom.cloud.sb.broker.config

import groovy.transform.CompileStatic
import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.commons.lang.builder.ToStringStyle

@CompileStatic
class UserConfig {
    String username
    String password
    String role
    String platformId

    @Override
    String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE)
    }
}
