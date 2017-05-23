package com.swisscom.cf.broker.cfextensions.serviceusage

import com.google.common.base.Optional
import com.swisscom.cf.servicebroker.model.usage.ServiceUsage
import com.swisscom.cf.broker.model.ServiceInstance

interface ServiceUsageProvider {
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate)
}