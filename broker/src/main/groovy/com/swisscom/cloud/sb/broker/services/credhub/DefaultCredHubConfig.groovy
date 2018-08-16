package com.swisscom.cloud.sb.broker.services.credhub

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.credhub")
class DefaultCredHubConfig implements CertificateConfig {
}
