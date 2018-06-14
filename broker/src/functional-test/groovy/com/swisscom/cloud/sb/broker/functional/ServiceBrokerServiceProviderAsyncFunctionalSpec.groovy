/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.TestableServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProvider
import com.swisscom.cloud.sb.client.model.LastOperationState

class ServiceBrokerServiceProviderAsyncFunctionalSpec extends BaseFunctionalSpec {

    private final int MAX_TIME_TO_WAIT_FOR_PROVISION_IN_SECONDS = 60

    private
    final String ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID = "asyncServiceBrokerServiceProviderInstanceId"
    private final String ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID = "asyncDummyServiceBrokerServiceInstanceId"

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private CFService dummyAsyncService
    private final String dummyAsyncPlanId = "dummyAsyncPlanId"
    private Plan dummyAsyncPlan

    void addParameters() {
        // parameters are added to plan which is used for the ServiceBrokerServiceProvider
        serviceLifeCycler.createParameter(BASE_URL, "http://localhost:8080", serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(USERNAME, cfAdminUser.username, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PASSWORD, cfAdminUser.password, serviceLifeCycler.plan)
    }

    def setup() {
        dummyAsyncPlan = new Plan(guid: dummyAsyncPlanId, name: "dummyAsyncServiceBrokerPlan", description: "Plan for AsyncDummyServiceBroker", asyncRequired: true)
        dummyAsyncService = serviceLifeCycler.createServiceIfDoesNotExist('AsyncDummyServiceProvider', ServiceProviderLookup.findInternalName(DummyServiceProvider.class), null, null, null, 0, false, false, null, null, null, dummyAsyncPlan)
        // creating service for TestableServiceBrokerServiceProvider
        serviceLifeCycler.createServiceIfDoesNotExist('dummyAsyncServiceProvider', ServiceProviderLookup.findInternalName(TestableServiceBrokerServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance"() {
        setup:
        addParameters()
        serviceLifeCycler.createParameter(SERVICE_ID, dummyAsyncService.guid, serviceLifeCycler.plan)
        serviceLifeCycler.createParameter(PLAN_ID, dummyAsyncPlan.guid, serviceLifeCycler.plan)
        serviceLifeCycler.setAsyncRequestInPlan(true)


        when:
        serviceLifeCycler.setServiceInstanceId(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.createServiceInstanceAndAssert(0, true, true)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION_IN_SECONDS, ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.setServiceInstanceId(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION_IN_SECONDS, ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
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
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION_IN_SECONDS, ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.getServiceInstanceStatus(ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID).state == LastOperationState.SUCCEEDED

        and:
        serviceLifeCycler.setServiceInstanceId(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION_IN_SECONDS, ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID)
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
        serviceLifeCycler.waitUntilMaxTimeOrTargetState(MAX_TIME_TO_WAIT_FOR_PROVISION_IN_SECONDS)
        assert serviceLifeCycler.getServiceInstanceStatus().state == LastOperationState.FAILED

        then:
        noExceptionThrown()

        cleanup:
        serviceLifeCycler.removeParameters()
    }
}
