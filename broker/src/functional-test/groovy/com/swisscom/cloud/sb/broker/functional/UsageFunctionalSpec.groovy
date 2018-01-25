package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider

class UsageFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('DummySynchronousService', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "it should get usage data for an existing service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        def response = serviceBrokerClient.getUsage(serviceLifeCycler.serviceInstanceId)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.value.length() > 0
    }


    def "it should get usage data for a deleted service instance"() {
        given:
        serviceLifeCycler.deleteServiceInstanceAndAssert(false)

        when:
        def response = serviceBrokerClient.getUsage(serviceLifeCycler.serviceInstanceId)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.value.length() > 0
    }
}