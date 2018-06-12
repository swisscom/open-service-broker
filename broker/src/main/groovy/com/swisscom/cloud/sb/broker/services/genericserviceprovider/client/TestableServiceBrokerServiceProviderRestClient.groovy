package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.services.genericserviceprovider.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
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
