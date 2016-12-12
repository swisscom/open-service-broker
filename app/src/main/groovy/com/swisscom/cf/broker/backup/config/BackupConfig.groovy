package com.swisscom.cf.broker.backup.config

import com.swisscom.cf.broker.config.Config
import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@CompileStatic
@Configuration
@ConfigurationProperties(prefix = 'com.swisscom.cf.broker.backup')
class BackupConfig implements Config {
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
}
