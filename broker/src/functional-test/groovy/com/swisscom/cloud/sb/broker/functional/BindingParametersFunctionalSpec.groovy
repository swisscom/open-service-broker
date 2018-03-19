package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class BindingParametersFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind with parameters"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])

        then:
        noExceptionThrown()
    }

    def "provision async service instance and bind with parameters with bindings not retrievable"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, false, false)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)

        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])
        serviceBrokerClient.getServiceInstanceBinding(serviceInstanceGuid, serviceBindingGuid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.NOT_FOUND
    }

    def "provision async service instance and bind with parameters with bindings retrievable"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, true, true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])
        def bindingResponse = serviceBrokerClient.getServiceInstanceBinding(serviceInstanceGuid, serviceBindingGuid)

        then:
        noExceptionThrown()
        bindingResponse != null
        bindingResponse.body.credentials != null
    }


    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()

        then:
        noExceptionThrown()
    }
}