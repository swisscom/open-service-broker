package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState

class AsyncServiceFunctionalSpec extends BaseFunctionalSpec {
    private int processDelayInSeconds = DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 2

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance"() {
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(true, true, ['delay': String.valueOf(processDelayInSeconds)])
        waitBasedOnServiceConfiguration()
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }

    private waitBasedOnServiceConfiguration() {
        serviceLifeCycler.pauseExecution(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true)
        waitBasedOnServiceConfiguration()
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }
}