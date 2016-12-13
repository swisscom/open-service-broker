package com.swisscom.cf.broker.services.common

import com.swisscom.cf.servicebroker.model.endpoint.Endpoint
import com.swisscom.cf.broker.model.ServiceInstance

interface EndpointProvider {
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance)
}