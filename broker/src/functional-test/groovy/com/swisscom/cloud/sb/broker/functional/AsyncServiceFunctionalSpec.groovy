package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.beans.factory.annotation.Autowired

class AsyncServiceFunctionalSpec extends BaseFunctionalSpec {
    private int processDelayInSeconds = DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2

    private static final String serviceInstanceAlias = "service-instance-a"

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true, ['delay': String.valueOf(processDelayInSeconds)])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }

    def "provision async service instance with alias"() {
        when:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())

        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'alias': serviceInstanceAlias])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        def si = serviceInstanceRepository.findByGuid(serviceLifeCycler.serviceInstanceId)
        assert si != null
        assert si.details.find { it.key == 'alias' && it.value == serviceInstanceAlias } != null
    }

    def "provision async service instance with parent"() {
        when:
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.setServiceBindingId(UUID.randomUUID().toString())

        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true,
                ['delay': String.valueOf(processDelayInSeconds), 'parentAlias': serviceInstanceAlias])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED

        def si = serviceInstanceRepository.findByGuid(serviceLifeCycler.serviceInstanceId)
        assert si != null
        assert si.parentServiceInstance != null
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }

}