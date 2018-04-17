package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.TestableServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
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
        dummySyncService = serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, false, false, null, null, null, dummySyncPlan)
        serviceLifeCycler.createServiceIfDoesNotExist('dummySyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))

        dummyAsyncPlan = new Plan(guid: dummyAsyncPlanId, name: "dummyAsyncServiceBrokerPlan", description: "Plan for AsyncDummyServiceBroker", asyncRequired: true )
        dummyAsyncService = serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class), null, null, null, 0, false, false, null, null, null, dummyAsyncPlan)
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

    def "get usage of provisioned service"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.setServiceInstanceId(DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)

        when:
        def response = serviceBrokerClient.getUsage(DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.value.length() > 0
    }

    def "provision same sync service instance as above for conflict"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        then:
        HttpClientErrorException e = thrown()
        e.statusCode == HttpStatus.CONFLICT

        cleanup:
        //for cleanup the id of the service instance provisioned via the sbsp needs to be added to the set of serviceInstanceIds
        serviceLifeCycler.setServiceInstanceId(DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)

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
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.DEFAULT_PROCESSING_DELAY_IN_SECONDS + 30 , true, true)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(60, DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.setServiceInstanceId(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(60, ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID).state == LastOperationState.SUCCEEDED

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
        serviceLifeCycler.setServiceInstanceId(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(50, ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID).state == LastOperationState.SUCCEEDED
    }

    def "failing provision of async service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(true)
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(DummyServiceProvider.DEFAULT_PROCESSING_DELAY_IN_SECONDS, true, true, ['success': false])
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(50)
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED

        then:
        noExceptionThrown()
    }
}
