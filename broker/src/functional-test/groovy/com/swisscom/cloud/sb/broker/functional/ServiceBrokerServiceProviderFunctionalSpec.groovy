package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.TestableServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ServiceBrokerServiceProviderFunctionalSpec extends BaseFunctionalSpec {

    private final int MAX_TIME_TO_WAIT_FOR_PROVISION = 60

    private final String SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID = "syncServiceBrokerServiceProviderInstanceId"
    private final String ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID = "asyncServiceBrokerServiceProviderInstanceId"
    private final String ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID = "asyncDummyServiceBrokerServiceInstanceId"

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private CFService dummySyncService
    private final String dummySyncPlanId = "dummySyncPlanId"
    private Plan dummySyncPlan

    private CFService dummyAsyncService
    private final String dummyAsyncPlanId = "dummyAsyncPlanId"
    private Plan dummyAsyncPlan

    void addParameters() {
        // parameters are added to plan which is used for the ServiceBrokerServiceProvider
        serviceLifeCycler.createParameter(BASE_URL,"http://localhost:8080", serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(USERNAME, cfAdminUser.username, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PASSWORD,cfAdminUser.password, serviceLifeCycler.plan)
    }

    def setup() {
        dummySyncPlan = new Plan(guid: dummySyncPlanId, name: "syncDummyServiceBrokerPlan", description: "Plan for SyncDummyServiceBroker", asyncRequired: false )
        dummySyncService = serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyServiceProvider', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, false, false, null, null, null, dummySyncPlan)
        // creating service for TestableServiceBrokerServiceProvider
        serviceLifeCycler.createServiceIfDoesNotExist('syncDummyServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))

        dummyAsyncPlan = new Plan(guid: dummyAsyncPlanId, name: "dummyAsyncServiceBrokerPlan", description: "Plan for AsyncDummyServiceBroker", asyncRequired: true )
        dummyAsyncService = serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyServiceProvider', ServiceProviderLookup.findInternalName(DummyServiceProvider.class), null, null, null, 0, false, false, null, null, null, dummyAsyncPlan)
        // creating service for TestableServiceBrokerServiceProvider
        serviceLifeCycler.createServiceIfDoesNotExist('dummyAsyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))
    }

    def "provision and bind sync service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)
        serviceLifeCycler.setAsyncRequestInPlan(false)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        and:
        serviceLifeCycler.setServiceInstanceId(SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.createServiceBindingAndAssert(0, false, false, null)

        then:
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.removeParameters()
    }

    def "get usage of provisioned service"() {
        given:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.setServiceInstanceId(SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID)

        when:
        def response = serviceBrokerClient.getUsage(SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID)

        then:
        response.statusCode.'2xxSuccessful'
        response.body.value.length() > 0
    }

    def "provision same sync service instance as above for conflict"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)
        serviceLifeCycler.setAsyncRequestInPlan(false)

        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        then:
        HttpClientErrorException e = thrown()
        e.statusCode == HttpStatus.CONFLICT

        cleanup:
        //for cleanup the id of the service instance provisioned via the sbsp needs to be added to the set of serviceInstanceIds
        serviceLifeCycler.setServiceInstanceId(SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.removeParameters()
    }

    def "unbind and deprovision sync service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)

        and:
        serviceLifeCycler.setAsyncRequestInPlan(false)
        serviceLifeCycler.setServiceInstanceId(SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.deleteServiceBindingAndAssert(null)

        when:
        serviceLifeCycler.deleteServiceInstanceAndAssert()

        then:
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.removeParameters()
    }


    def "provision async service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)
        serviceLifeCycler.setAsyncRequestInPlan(true)


        when:
        serviceLifeCycler.setServiceInstanceId(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.createServiceInstanceAndAssert(0 , true, true)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION, ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.setServiceInstanceId(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION, ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        then:
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.removeParameters()
    }

    def "deprovision async service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)
        serviceLifeCycler.setAsyncRequestInPlan(true)


        when:
        serviceLifeCycler.setServiceInstanceId(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.deleteServiceInstanceAndAssert(true, DummyServiceProvider.DEFAULT_PROCESSING_DELAY_IN_SECONDS + 50)

        then:
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION, ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.setServiceInstanceId(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION, ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        cleanup:
        serviceLifeCycler.removeParameters()
    }

    def "failing provision of async service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)

        and:
        serviceLifeCycler.setAsyncRequestInPlan(true)
        serviceLifeCycler.setServiceInstanceId(UUID.randomUUID().toString())


        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, true, true, ['success': false])
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION)
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED

        then:
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.removeParameters()
    }
}
