package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ServiceContext
import com.swisscom.cloud.sb.broker.model.ServiceInstance

interface ContextRepository extends BaseRepository<ServiceContext, Integer> {
    ServiceContext findByKeyAndServiceInstance(String key, ServiceInstance serviceInstance)
}
