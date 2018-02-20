package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyFailingServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState

class FirstAsyncProvisioningStepFailsFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyFailing', ServiceProviderLookup.findInternalName(DummyFailingServiceProvider))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Service Instance is created when async provision request returned HttpStatus.ACCEPTED even first async step fails"() {
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyFailingServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4, true, true, ['delay': String.valueOf(DummyFailingServiceProvider.RETRY_INTERVAL_IN_SECONDS)])
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED
    }

    def "Failed Service Instance can be deleted"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyFailingServiceProvider.RETRY_INTERVAL_IN_SECONDS * 4)
        then:
        noExceptionThrown()
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }
}