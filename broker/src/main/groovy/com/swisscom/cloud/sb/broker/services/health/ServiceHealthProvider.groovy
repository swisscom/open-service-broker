package com.swisscom.cloud.sb.broker.services.health

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.model.health.ServiceHealth

interface ServiceHealthProvider {
    ServiceHealth getHealth(ServiceInstance serviceInstance)
}
