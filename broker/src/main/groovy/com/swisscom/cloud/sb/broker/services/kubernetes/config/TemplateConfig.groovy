package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.config.Config
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.swisscom.cloud.sb.broker")
class TemplateConfig implements Config {
    List<ServiceTemplate> serviceTemplates

    static class ServiceTemplate {
        String name
        String version
        List<String> templates
    }

    List<String> getTemplateForServiceKey(String templateUniqueIdentifier) {
        def matchedServiceTemplates = serviceTemplates.findAll { it.name == templateUniqueIdentifier}
        matchedServiceTemplates.first()?.templates
    }

    List<String> getTemplateForServiceKey(String templateUniqueIdentifier, String templateVersion) {
        def matchedServiceTemplates = serviceTemplates.findAll { it.name == templateUniqueIdentifier && it.version == templateVersion }
        matchedServiceTemplates.first()?.templates
    }
}
