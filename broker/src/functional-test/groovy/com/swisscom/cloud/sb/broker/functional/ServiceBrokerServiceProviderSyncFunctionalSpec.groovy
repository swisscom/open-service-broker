package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.TestableServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ServiceBrokerServiceProviderSyncFunctionalSpec extends BaseFunctionalSpec {
    private final String SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID = "syncServiceBrokerServiceProviderInstanceId"

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private CFService dummySyncService
    private final String dummySyncPlanId = "dummySyncPlanId"
    private Plan dummySyncPlan

    void addParameters() {
        // parameters are added to plan which is used for the ServiceBrokerServiceProvider
        serviceLifeCycler.createParameter(BASE_URL,"http://localhost:8080", serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(USERNAME, cfAdminUser.username, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PASSWORD,cfAdminUser.password, serviceLifeCycler.plan)
    }

    def setup() {
        dummySyncPlan = new Plan(guid: dummySyncPlanId, name: "syncDummyServiceBrokerPlan", description: "Plan for SyncDummyServiceBroker", asyncRequired: false)
        dummySyncService = serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyServiceProvider', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, false, false, null, null, null, dummySyncPlan)
        // creating service for TestableServiceBrokerServiceProvider
        serviceLifeCycler.createServiceIfDoesNotExist('syncDummyServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision and bind sync service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummySyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummySyncPlan.guid, serviceLifeCycler.plan)
        serviceLifeCycler.setAsyncRequestInPlan(false)

        when:
        serviceLifeCycler.setServiceInstanceId(SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        and:
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
}
