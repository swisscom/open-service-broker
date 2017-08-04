package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.ServiceLifeCycler
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider

class EndpointLookupFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyServiceManagerBased', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "should get a non empty response for endpoints of a service manager based service"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        def response = serviceBrokerClient.getEndpoint(serviceLifeCycler.serviceInstanceId)
        then:
        response.statusCode.'2xxSuccessful'
        response.body.size() > 0

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(false)
    }

    def "should get an empty response for a *NON* service manager based service"() {
        given:
        ServiceLifeCycler lifeCycler = applicationContext.getBean(ServiceLifeCycler.class)
        lifeCycler.createServiceIfDoesNotExist('SynchronousDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
        lifeCycler.createServiceInstanceAndAssert(0, false, false)
        when:
        def response = serviceBrokerClient.getEndpoint(lifeCycler.serviceInstanceId)
        then:
        response.statusCode.'2xxSuccessful'
        response.body.size() == 0

        cleanup:
        lifeCycler.deleteServiceInstanceAndAssert(false)
        lifeCycler.cleanup()
    }

}