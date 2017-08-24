package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import com.swisscom.cloud.sb.broker.services.kubernetes.config.TemplateConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Log4j
@Component
@CompileStatic
class KubernetesTemplateManager {
    private final TemplateConfig templateConfig

    @Autowired
    KubernetesTemplateManager(TemplateConfig templateConfig) {
        this.templateConfig = templateConfig
    }

    List<KubernetesTemplate> getTemplates(String templateUniqueIdentifier) {
        return splitTemplatesFromYamlDoucments(templateConfig.getTemplateForServiceKey(templateUniqueIdentifier))
    }

    List<KubernetesTemplate> getTemplates(String templateUniqueIdentifier, String templateVersion) {
        return splitTemplatesFromYamlDoucments(templateConfig.getTemplateForServiceKey(templateUniqueIdentifier, templateVersion))
    }

    private List<KubernetesTemplate> splitTemplatesFromYamlDoucments(List<String> templates) {
        def deploymentTemplates = templates.collect{it.split("---")}.flatten()
        return deploymentTemplates.collect{new KubernetesTemplate(it as String)}
    }
}
