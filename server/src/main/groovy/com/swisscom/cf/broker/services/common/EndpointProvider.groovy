package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.cfextensions.endpoint.EndpointDto
import com.swisscom.cf.broker.model.ServiceInstance

interface EndpointProvider {
    Collection<EndpointDto> findEndpoints(ServiceInstance serviceInstance)
}