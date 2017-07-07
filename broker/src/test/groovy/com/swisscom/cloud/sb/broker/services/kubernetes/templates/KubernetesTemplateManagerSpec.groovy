package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import spock.lang.Specification

class KubernetesTemplateManagerSpec extends Specification {


    KubernetesTemplateManager kubernetesTemplateManager
    KubernetesConfig kubernetesConfig

    def setup() {
        kubernetesConfig = Mock()
        and:
        kubernetesTemplateManager = new KubernetesTemplateManager(kubernetesConfig)
    }


}
