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

package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.services.genericserviceprovider.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.http.ResponseEntity

class TestableServiceBrokerServiceProviderRestClient extends ServiceBrokerServiceProviderRestClient {

    private final String ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID = "asyncDummyServiceBrokerServiceInstanceId"

    @Override
    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, CreateServiceInstanceRequest createServiceInstanceRequest, String serviceInstanceId) {
        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID).withAsyncAccepted(true))
    }

    @Override
    ResponseEntity<Void> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, String serviceInstanceId, GenericProvisionRequestPlanParameter req) {
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(ASYNC_DUMMY_SERVICE_BROKER_SERVICE_INSTANCE_ID, req.serviceId, req.planId, true)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }
}