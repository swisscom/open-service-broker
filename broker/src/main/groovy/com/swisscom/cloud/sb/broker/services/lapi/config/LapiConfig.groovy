package com.swisscom.cloud.sb.broker.services.lapi.config

import com.swisscom.cloud.sb.broker.config.Config
import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cloud.sb.broker.service.lapi')
class LapiConfig implements Config {
    String lapiUsername
    String lapiPassword
}
