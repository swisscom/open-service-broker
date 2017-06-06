package com.swisscom.cloud.sb.broker.cfextensions.serviceusage

import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.model.usage.ServiceUsage

interface ServiceUsageProvider {
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate)
}