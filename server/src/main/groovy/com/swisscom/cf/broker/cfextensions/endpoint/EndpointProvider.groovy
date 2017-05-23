package com.swisscom.cf.broker.cfextensions.endpoint

import com.swisscom.cf.servicebroker.model.endpoint.Endpoint
import com.swisscom.cf.broker.model.ServiceInstance

interface EndpointProvider {
    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance)
}