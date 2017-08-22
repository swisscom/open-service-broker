package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.config.Config
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker")
class TemplateConfig implements Config {
    Map<String, Object> templates

    List<Object> getTemplateForServiceKey(String key){
        return ((Map) templates.get(key)).values().asList()
    }
}
