package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.util.Resource
import groovy.util.logging.Log4j
import org.springframework.stereotype.Component

@Component
@Log4j
class KubernetesTemplateManager {

    private final KubernetesConfig kubernetesConfig

    KubernetesTemplateManager(KubernetesConfig kubernetesConfig) {
        this.kubernetesConfig = kubernetesConfig
    }

    KubernetesTemplate getNamespaceTemplate() {
        return new KubernetesTemplate(readTemplateContent("namespace.yml"))
    }

    KubernetesTemplate getServiceAccountsTemplate() {
        return new KubernetesTemplate(readTemplateContent("serviceaccount.yml"))
    }

    KubernetesTemplate getServiceRolesTemplate() {
        return new KubernetesTemplate(readTemplateContent("roles.yml"))
    }

    private String readTemplateContent(String templateIdentifier) {
        File file = new File(kubernetesConfig.getKubernetesTemplatesFolder(), templateIdentifier)
        if (file.exists()) {
            log.info("Using template file:${file.absolutePath}")
            return file.text
        }
        log.info("Will try to read file:${templateIdentifier} from embedded resources")
        return Resource.readTestFileContent("/kubernetes/redis/v1/" + templateIdentifier)
    }

}
