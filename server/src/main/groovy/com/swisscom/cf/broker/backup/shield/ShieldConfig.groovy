package com.swisscom.cf.broker.backup.shield

import com.swisscom.cf.broker.config.Config
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cf.broker.shield")
class ShieldConfig implements Config {
    String baseUrl
    String apiKey
    String agent

    String jobPrefix
    String targetPrefix
    String storeName
    String retentionName
    String scheduleName
}
