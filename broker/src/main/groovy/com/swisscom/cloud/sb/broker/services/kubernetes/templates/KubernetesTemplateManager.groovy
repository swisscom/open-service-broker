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
    private final AbstractKubernetesServiceConfig kubernetesServiceConfig
    private final TemplateConfig templateConfig

    @Autowired
    KubernetesTemplateManager(AbstractKubernetesServiceConfig kubernetesServiceConfig, TemplateConfig templateConfig) {
        this.kubernetesServiceConfig = kubernetesServiceConfig
        this.templateConfig = templateConfig
    }

    List<KubernetesTemplate> getTemplates() {
        def deploymentTemplates = templateConfig.getTemplateForServiceKey(kubernetesServiceConfig.templateKey).collect{it.split("---")}.flatten()
        return deploymentTemplates.collect{new KubernetesTemplate(it as String)}
    }
}
