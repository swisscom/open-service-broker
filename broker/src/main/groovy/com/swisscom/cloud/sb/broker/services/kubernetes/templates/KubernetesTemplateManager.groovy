package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.kubernetes.config.AbstractKubernetesServiceConfig
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml


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
        Yaml yaml = new Yaml()
        return templateConfig.getTemplateForServiceKey(kubernetesServiceConfig.templateKey).collect{new KubernetesTemplate(yaml.dump(it))}
    }
}
