package com.swisscom.cf.broker.cfextensions.endpoint

import com.swisscom.cf.broker.model.CFService
import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.services.common.EndpointProvider
import com.swisscom.cf.broker.services.common.ServiceProvider
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import spock.lang.Specification

class EndpointLookupSpec extends Specification {

    private EndpointService endpointLookup

    def setup() {
        endpointLookup = new EndpointService()
    }

    def 'getting endpoint for a *NON* EndpointProvider based service instance functions correctly'() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(plan: new Plan(templateUniqueIdentifier: null, service: new CFService()))
        and:
        endpointLookup.serviceProviderLookup = Mock(ServiceProviderLookup)
        endpointLookup.serviceProviderLookup.findServiceProvider(serviceInstance.plan) >> Stub(ServiceProvider)
        when:
        def list = endpointLookup.lookup(serviceInstance)
        then:
        list.size() == 0
    }

    def 'getting endpoint for an EndpointProvider based service instance functions correctly'() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(plan: new Plan(templateUniqueIdentifier: null, service: new CFService()))
        and:
        endpointLookup.serviceProviderLookup = Mock(ServiceProviderLookup)
        def serviceProvider = Mock(ServiceProviderWithEndpointProvider)
        serviceProvider.findEndpoints(serviceInstance) >> [new Endpoint()]
        endpointLookup.serviceProviderLookup.findServiceProvider(serviceInstance.plan) >> serviceProvider
        when:
        def list = endpointLookup.lookup(serviceInstance)
        then:
        list.size() == 1
    }

    private static interface ServiceProviderWithEndpointProvider extends ServiceProvider, EndpointProvider {}


}
