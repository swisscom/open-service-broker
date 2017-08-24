package com.swisscom.cloud.sb.broker.services.kubernetes.config

import com.swisscom.cloud.sb.broker.config.Config
import com.swisscom.cloud.sb.broker.services.AsyncServiceConfig

trait AbstractKubernetesServiceConfig implements Config, AsyncServiceConfig {
    boolean enablePodLabelHealthzFilter
}
