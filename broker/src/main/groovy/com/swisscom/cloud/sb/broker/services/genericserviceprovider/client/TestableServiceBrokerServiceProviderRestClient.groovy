package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse
import org.springframework.http.ResponseEntity

class TestableServiceBrokerServiceProviderRestClient extends ServiceBrokerServiceProviderRestClient {

    private final ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID = "asyncServiceInstanceToBeBoundId"

    @Override
    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, CreateServiceInstanceRequest createServiceInstanceRequest, String serviceInstanceId) {
        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID).withAsyncAccepted(true))
    }

    @Override
    ResponseEntity<Void> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, String serviceInstanceId, GenericProvisionRequestPlanParameter req) {
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(ASYNC_SERVICE_INSTANCE_TO_BE_BOUND_ID, req.serviceId, req.planId, true)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }
}
