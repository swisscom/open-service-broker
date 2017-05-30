package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.model.endpoint.Endpoint

interface EndpointProvider {
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance)
}