package com.swisscom.cloud.sb.broker.services.kubernetes.templates

import com.swisscom.cloud.sb.broker.services.kubernetes.redis.config.KubernetesRedisConfig
import spock.lang.Specification

class KubernetesTemplateManagerSpec extends Specification {


    KubernetesTemplateManager kubernetesTemplateManager
    KubernetesRedisConfig kubernetesConfig

    def setup() {
        kubernetesConfig = Mock()
        and:
        kubernetesTemplateManager = new KubernetesTemplateManager(kubernetesConfig)
    }


}
