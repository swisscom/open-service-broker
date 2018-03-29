package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.TestableServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import sun.reflect.generics.reflectiveObjects.NotImplementedException

class ServiceBrokerServiceProviderFunctionalSpec extends BaseFunctionalSpec {

    private final String DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID = "dummySyncServiceBrokerInstanceId";
    private final String DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID = "dummyAsyncServiceBrokerInstanceId";
    private final String ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID = "asyncServiceInstanceToBeBoundId"

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private CFService dummySyncService
    private Plan dummySyncPlan

    private CFService dummyAsyncService
    private final String dummySyncPlanId = "dummySyncPlanId"
    private Plan dummyAsyncPlan
    private final String dummyAsyncPlanId = "dummyAsyncPlanId"

    def setup() {
        dummySyncPlan = new Plan(guid: dummySyncPlanId, name: "dummySyncServiceBrokerPlan", description: "Plan for SyncDummyServiceBroker", asyncRequired: false )
        dummySyncService = serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, dummySyncPlan)
        serviceLifeCycler.createServiceIfDoesNotExist('dummySyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))

        dummyAsyncPlan = new Plan(guid: dummyAsyncPlanId, name: "dummyAsyncServiceBrokerPlan", description: "Plan for AsyncDummyServiceBroker", asyncRequired: true )
        dummyAsyncService = serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class), null, null, null, 0, dummyAsyncPlan)
        serviceLifeCycler.createServiceIfDoesNotExist('dummyAsyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))

        serviceLifeCycler.createParameter(BASE_URL,"http://localhost:8080", serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(USERNAME, cfAdminUser.username, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PASSWORD,cfAdminUser.password, serviceLifeCycler.plan)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision and bind sync service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        and:
        serviceLifeCycler.setServiceInstanceId(DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.createServiceBindingAndAssert(0, false, false, null)

        then:
        noExceptionThrown()
    }

    def "unbind and deprovision sync service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.setServiceInstanceId(DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.deleteServiceBindingAndAssert(null)

        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }

    def "provision async service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(true)
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.setServiceInstanceId(DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.DEFAULT_PROCESSING_DELAY_IN_SECONDS + 40, true, true, ['delay': String.valueOf(DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS + 30)])
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(30, DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(30, ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID)
        assert serviceLifeCycler.serviceInstanceRepository.findByGuid(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID).completed

        then:
        noExceptionThrown()
    }

    def "deprovision async service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(true)
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.setServiceInstanceId(DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.DEFAULT_PROCESSING_DELAY_IN_SECONDS + 50)

        then:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(30, DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(30, ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID).state == LastOperationState.SUCCEEDED
    }
}
