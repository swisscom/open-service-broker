package com.swisscom.cf.broker.services.common

import com.google.common.base.Optional
import com.swisscom.cf.broker.cfextensions.serviceusage.ServiceUsage
import com.swisscom.cf.broker.model.ServiceInstance

interface ServiceUsageProvider {
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate)
}