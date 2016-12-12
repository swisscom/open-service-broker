package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.filterextensions.endpoint.EndpointDto
import com.swisscom.cf.broker.model.ServiceInstance

interface EndpointProvider {
    Collection<EndpointDto> findEndpoints(ServiceInstance serviceInstance)
}