package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.config.Config
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker.shield")
class ShieldConfig implements Config {
    String baseUrl
    String apiKey
    String jobPrefix
    String targetPrefix
}
