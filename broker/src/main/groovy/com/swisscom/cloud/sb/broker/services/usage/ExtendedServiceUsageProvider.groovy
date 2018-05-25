package com.swisscom.cloud.sb.broker.services.usage

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.model.usage.extended.ServiceUsageItem

interface ExtendedServiceUsageProvider {
    Set<ServiceUsageItem> getUsages(ServiceInstance serviceInstance)
}
