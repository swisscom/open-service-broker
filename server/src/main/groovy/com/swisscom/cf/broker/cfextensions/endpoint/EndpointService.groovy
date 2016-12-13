package com.swisscom.cf.broker.cfextensions.endpoint

import com.google.common.base.Preconditions
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.services.common.EndpointProvider
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import com.swisscom.cf.servicebroker.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@CompileStatic
@Service
class EndpointService {
    @Autowired
    protected ServiceProviderLookup serviceProviderLookup

    Collection<Endpoint> lookup(ServiceInstance serviceInstance) {
        Preconditions.checkNotNull(serviceInstance, "A valid ServiceInstance argument is required")
        Preconditions.checkNotNull(serviceInstance.plan, "A valid plan on ServiceInstance argument is required")

        def serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (serviceProvider instanceof EndpointProvider) {
            return (serviceProvider as EndpointProvider).findEndpoints(serviceInstance)
        } else {
            return []
        }
    }
}
