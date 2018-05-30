package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class BindingParametersFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind with parameters"() {
        given:
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])

        then:
        noExceptionThrown()

        def serviceBinding = serviceBindingRepository.findByGuid(serviceBindingGuid)
        serviceBinding != null
        serviceBinding.applicationUser.username == cfAdminUser.username
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
        bindingResponse.body.parameters != null
    }

    def "provision async service instance and fetch non existing binding"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, true, true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceBrokerClient.getServiceInstanceBinding(serviceInstanceGuid, serviceBindingGuid)

        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.NOT_FOUND
    }

    def "provision async service instance and unbind non existing binding"() {
        given:
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, true, true)

        def serviceInstanceGuid = UUID.randomUUID().toString()
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false,)

        when:
        serviceBrokerClient.deleteServiceInstanceBinding(new DeleteServiceInstanceBindingRequest(serviceInstanceGuid,
                serviceBindingGuid, serviceLifeCycler.cfService.guid, serviceLifeCycler.cfService.plans[0].guid))
        then:
        def ex = thrown(HttpClientErrorException)
        ex.statusCode == HttpStatus.GONE
    }

    def "deprovision async service instance"() {
        given:
        def serviceInstanceGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndServiceBindingAndAssert()

        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }
}