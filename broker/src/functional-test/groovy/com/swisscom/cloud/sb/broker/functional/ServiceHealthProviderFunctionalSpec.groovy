package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceHealthServiceProvider
import com.swisscom.cloud.sb.model.health.ServiceHealthStatus

class ServiceHealthProviderFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist(
                'DummyServiceProvider',
                ServiceProviderLookup.findInternalName(DummyServiceHealthServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Can get Health informations for service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        def response = serviceBrokerClient.getHealth(serviceLifeCycler.serviceInstanceId)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.status == ServiceHealthStatus.OK
    }
}
