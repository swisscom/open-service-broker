package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

/**
 * This class has been created in order to be able to run functional tests on the ServiceBrokerServiceProvider.
 * In the edge case, that the ServiceBrokerServiceProvider is used to provision a service on http://localhost:8080
 * we have the issue, that the ServiceBrokerServiceProvider is run through twice with the same serviceInstanceId
 * provided via the URL. This is a problem because then the same serviceInstanceId is attempted to be inserted into the
 * same DB twice which results in an Exception thrown by the Hibernate DB. In this class the method which handles the
 * ServiceInstanceId forwarding is overwritten, so that the ServiceInstanceId is generated randomly for test cases
 * thereby avoiding the above described issue.
 */
@Component("testableServiceBrokerServiceProvider")
class TestableServiceBrokerServiceProvider extends ServiceBrokerServiceProvider{

    private final static String DUMMY_SYNC_SERVICE_BROKER_INSTANCE_ID = "dummySyncServiceBrokerInstanceId"
    private final static String DUMMY_ASYNC_SERVICE_BROKER_INSTANCE_ID = "dummyAsyncServiceBrokerInstanceId"

    // This method differs from its original counterpart in so far that the serviceInstanceId that is passed to the
    // createServiceInstanceRequest is generate randomly rather than taken from the request to avoid duplicate serviceInstanceId
    @Override
    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(CreateServiceInstanceRequest createServiceInstanceRequest, ProvisionRequest request) {
        def serviceInstanceId
        if(request.plan.asyncRequired) {
            serviceInstanceId = DUMMY_ASYNC_SERVICE_BROKER_INSTANCE_ID
        } else {
            serviceInstanceId = DUMMY_SYNC_SERVICE_BROKER_INSTANCE_ID
        }
        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(serviceInstanceId).withAsyncAccepted(request.acceptsIncomplete))
    }

    @Override
    ResponseEntity<DeleteServiceInstanceResponse> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, DeprovisionRequest request, GenericProvisionRequestPlanParameter req) {
        def serviceInstanceId
        if(request.serviceInstance.plan.asyncRequired) {
            serviceInstanceId = DUMMY_ASYNC_SERVICE_BROKER_INSTANCE_ID
        } else {
            serviceInstanceId = DUMMY_SYNC_SERVICE_BROKER_INSTANCE_ID
        }
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(serviceInstanceId, req.serviceId, req.planId, request.acceptsIncomplete)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }
}
