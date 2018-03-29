package com.swisscom.cloud.sb.broker.updating

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import spock.lang.Specification

class UpdatingServiceSpec extends Specification {

    protected ServiceProviderLookup serviceProviderLookup
    protected UpdatingPersistenceService updatingPersistenceService;
    ServiceInstance serviceInstance

    def setup() {
        serviceProviderLookup = Mock(ServiceProviderLookup)
        updatingPersistenceService = Mock(UpdatingPersistenceService)
        serviceInstance = Mock(ServiceInstance)

        def dummyServiceProvider = new DummyServiceProvider()
        serviceProviderLookup.findServiceProvider(_) >> dummyServiceProvider
    }

    def "DummyServiceProvider.update responds with ServiceDetails"() {

        def dummyServiceProvider = new DummyServiceProvider()
        def updateRequest = new UpdateRequest(parameters: "{\"mode\":\"stopped\"}")

        when:
        def updateResponse = dummyServiceProvider.update(updateRequest)

        then:
        updateResponse.details.find { it -> it.key == "mode" && it.value == "stopped" } != null
    }

    def "Throws Exception if plan changes and async validation is not valid"() {
        def sut = new UpdatingService(serviceProviderLookup, updatingPersistenceService)
        def updateRequest = new UpdateRequest(
                plan: new Plan(guid: "124654a0-f015-4e59-b806-c19f425c7a51", asyncRequired: true, service: new CFService()),
                previousPlan: new Plan(guid: "51465719-0456-4f8a-afd5-2db6d189baf8", asyncRequired: true, service: new CFService()),
        )
        def asynchronous = false

        when:
        sut.update(serviceInstance, updateRequest, asynchronous)

        then:
        def exception = thrown(Exception)
        exception.message == ErrorCode.ASYNC_REQUIRED.description
    }

    def "Throws No Exception if plan doesn't change and async validation is not valid"() {
        def sut = new UpdatingService(serviceProviderLookup, updatingPersistenceService)
        def updateRequest = new UpdateRequest(
                plan: new Plan(guid: "124654a0-f015-4e59-b806-c19f425c7a51", asyncRequired: true, service: new CFService()),
                previousPlan: new Plan(guid: "124654a0-f015-4e59-b806-c19f425c7a51", asyncRequired: true, service: new CFService()),
        )
        def asynchronous = false

        when:
        sut.update(serviceInstance, updateRequest, asynchronous)

        then:
        noExceptionThrown()
    }

    def "Throws No Exception if plan changes and async validation is valid"() {
        def sut = new UpdatingService(serviceProviderLookup, updatingPersistenceService)
        def updateRequest = new UpdateRequest(
                plan: new Plan(guid: "124654a0-f015-4e59-b806-c19f425c7a51", asyncRequired: false, service: new CFService(plan_updateable: true)),
                previousPlan: new Plan(guid: "51465719-0456-4f8a-afd5-2db6d189baf8", asyncRequired: false, service: new CFService(plan_updateable: true)),
        )
        def asynchronous = false

        when:
        sut.update(serviceInstance, updateRequest, asynchronous)

        then:
        noExceptionThrown()
    }


}
