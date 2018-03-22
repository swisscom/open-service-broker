package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.TestableServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState

class ServiceBrokerServiceProviderFunctionalSpec extends BaseFunctionalSpec {

    private final String DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID = "dummySyncServiceBrokerInstanceId";
    private final String DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID = "dummyAsyncServiceBrokerInstanceId";

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private CFService dummySyncService
    private Plan dummySyncPlan

    private CFService dummyAsyncService
    private Plan dummyAsyncPlan

    def setup() {
        def dummySyncPlanId = UUID.randomUUID().toString()
        dummySyncPlan = new Plan(guid: dummySyncPlanId, name: "dummySyncServiceBrokerPlan", description: "Plan for SyncDummyServiceBroker", asyncRequired: false )
        dummySyncService = serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, dummySyncPlan)
        serviceLifeCycler.createServiceIfDoesNotExist('dummySyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))

        def dummyAsyncPlanId = UUID.randomUUID().toString()
        dummyAsyncPlan = new Plan(guid: dummyAsyncPlanId, name: "dummyAsyncServiceBrokerPlan", description: "Plan for AsyncDummyServiceBroker", asyncRequired: true )
        dummyAsyncService = serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummy', ServiceProviderLookup.findInternalName(DummyServiceProvider.class), null, null, null, 0, dummyAsyncPlan)
        serviceLifeCycler.createServiceIfDoesNotExist('dummyAsyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))

        serviceLifeCycler.createParameter(BASE_URL,"http://localhost:8080", serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(USERNAME, cfAdminUser.username, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PASSWORD,cfAdminUser.password, serviceLifeCycler.plan)
    }

    def cleanupSpec() {
        //serviceLifeCycler.cleanup()
    }

    def "provision and bind sync service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        and:
        serviceLifeCycler.createServiceBindingAndAssert(0, false, false, DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID, null)

        then:
        noExceptionThrown()
    }

    def "unbind and deprovision sync service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.deleteServiceBindingAndAssert(null, DUMMY_SYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)

        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert()

        then:
        noExceptionThrown()
    }

    def "provision and bind async service instance"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(true)
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, true, true, DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID)

        and:
        //serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
        //serviceLifeCycler.createServiceBindingAndAssert(30, false, false, DUMMY_ASYNC_SERVICE_BROKER_SERVICE_INSTANCE_ID, null)

        then:
        noExceptionThrown()
    }



    /*def "provision async service instance"() {
        given:
        // add +30s because of the startup delay of the quartz scheduler
        serviceLifeCycler.createServiceInstanceAndAssert(maxSecondsToAwaitInstance: DummyServiceProvider.RETRY_INTERVAL_IN_SECONDS + 30, asyncRequest: true, asyncResponse: true, parameters: ['success': false])
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED
        when:
        def response = serviceLifeCycler.requestBindService('someKindaServiceInstanceGuid')
        then:
        def ex = thrown(HttpClientErrorException)
        ex.rawStatusCode == 412
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, 35)
        then:
        serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.SUCCEEDED
    }*/
}
