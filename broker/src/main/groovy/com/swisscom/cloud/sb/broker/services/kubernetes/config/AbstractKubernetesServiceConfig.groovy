package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.config.Config

trait AbstractKubernetesServiceConfig implements Config {
    boolean enablePodLabelHealthzFilter
    String templateKey
}
