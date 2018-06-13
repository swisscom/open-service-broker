package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

/**
 * This class has been created in order to be able to run functional tests on the ServiceBrokerServiceProvider.
 * In the edge case, that the ServiceBrokerServiceProvider is used to provision a service on http://localhost:8080
 * we have the issue, that the ServiceBrokerServiceProvider is run through twice with the same serviceInstanceId
 * provided via the URL. This is a problem because then the same serviceInstanceId is attempted to be inserted into the
 * same DB twice which results in an Exception thrown by the the unique constraint. In this class the method which handles
 * the ServiceInstanceId forwarding is overwritten, so that the ServiceInstanceId is generated randomly for test cases
 * thereby avoiding the above described issue.
 */
@Component("testableServiceBrokerServiceProvider")
class TestableServiceBrokerServiceProvider extends ServiceBrokerServiceProvider {

    private
    final String SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID = "syncServiceBrokerServiceProviderInstanceId"
    private
    final String ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID = "asyncServiceBrokerServiceProviderInstanceId"

    // This method differs from its original counterpart in so far that the serviceInstanceId that is passed to the
    // createServiceInstanceRequest is generate randomly rather than taken from the request to avoid duplicate serviceInstanceId
    @Override
    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(CreateServiceInstanceRequest createServiceInstanceRequest, ProvisionRequest request) {
        def serviceInstanceId
        if (request.plan.asyncRequired) {
            serviceInstanceId = ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID
        } else {
            serviceInstanceId = SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID
        }
        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(serviceInstanceId).withAsyncAccepted(request.acceptsIncomplete))
    }

    @Override
    ResponseEntity<Void> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, DeprovisionRequest request, GenericProvisionRequestPlanParameter req) {
        def serviceInstanceId
        if (request.serviceInstance.plan.asyncRequired) {
            serviceInstanceId = ASYNC_SERVICE_BROKER__SERVICE_PROVIDER_SERVICE_INSTANCE_ID
        } else {
            serviceInstanceId = SYNC_SERVICE_BROKER_SERVICE_PROVIDER_SERVICE_INSTANCE_ID
        }
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(serviceInstanceId, req.serviceId, req.planId, request.acceptsIncomplete)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }
}
