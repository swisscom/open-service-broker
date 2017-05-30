package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.swisscom.cloud.sb.model.endpoint.Endpoint
import com.swisscom.cloud.sb.broker.model.ServiceInstance

interface EndpointProvider {
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance)
}