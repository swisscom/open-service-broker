package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.model.endpoint.Endpoint
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

    def 'parsing single endpoint ip range'(){
        given:
        ServiceInstance serviceInstance = new ServiceInstance(plan: new Plan(templateUniqueIdentifier: null, service: new CFService()))
        serviceInstance.details.add(new ServiceDetail().from(ServiceDetailKey.PORT, '2222'))
        and:
        EndpointConfig config = new MongoDbEnterpriseConfig(ipRanges: new ArrayList<String>(), protocols: new ArrayList<String>())
        config.ipRanges.add('127.0.0.1')
        and:
        EndpointLookup el = new EndpointLookup()
        when:
        Collection<Endpoint> endpoints = el.findEndpoints(serviceInstance, config)
        then:
        endpoints.size() == 1
        endpoints.first().destination == '127.0.0.1'
        endpoints.first().protocol == 'tcp'
    }

    def 'parsing multiple endpoint ip ranges'(){
        given:
        ServiceInstance serviceInstance = new ServiceInstance(plan: new Plan(templateUniqueIdentifier: null, service: new CFService()))
        serviceInstance.details.add(new ServiceDetail().from(ServiceDetailKey.PORT, '2222'))
        and:
        EndpointConfig config = new MongoDbEnterpriseConfig(ipRanges: new ArrayList<String>(), protocols: new ArrayList<String>())
        config.ipRanges.addAll(['127.0.0.1', '127.0.0.2'])
        and:
        EndpointLookup el = new EndpointLookup()
        when:
        Collection<Endpoint> endpoints = el.findEndpoints(serviceInstance, config)
        then:
        endpoints.size() == 2
        endpoints.first().destination == '127.0.0.1'
        endpoints.first().protocol == 'tcp'
        endpoints[1].destination == '127.0.0.2'
    }

    private static interface ServiceProviderWithEndpointProvider extends ServiceProvider, EndpointProvider {}


}
