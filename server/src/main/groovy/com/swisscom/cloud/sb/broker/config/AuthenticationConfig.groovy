package com.swisscom.cloud.sb.broker.config

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cf.broker.security')
class AuthenticationConfig {
    String cfUsername
    String cfPassword
    String cfExtUsername
    String cfExtPassword
}
