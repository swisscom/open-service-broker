package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.ServiceBrokerServiceProvider
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.config.ServiceBrokerServiceProviderConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ServiceBrokerServiceProviderRestClient {

    // maybe put url into sbspConfig?
    private ServiceBrokerServiceProviderConfig sbspConfig
    private RestTemplateBuilder restTemplateBuilder

    @Autowired
    EceRestClient(ServiceBrokerServiceProviderConfig sbspConfig, RestTemplateBuilder restTemplateBuilder) {
        this.sbspConfig = sbspConfig
        this.restTemplateBuilder = restTemplateBuilder
    }

    // If the provisioning fails with an exception, the exception is caught in the execute method of the AbstractLastOperationJob
    // which will result in the LastOperation status being set to failed
    boolean provisionServiceInstance(ServiceInstance serviceInstance) {
        log.info("Async provisioning of service instance with id ${serviceInstance.guid}")
        GenericProvisionRequestPlanParameter req = ServiceBrokerServiceProvider.populateGenericProvisionRequestPlanParameter(serviceInstance.plan.parameters)
        def createServiceInstanceRequest = new CreateServiceInstanceRequest(req.serviceId, req.planId, null, null, null)
        ServiceBrokerClient serviceBrokerClient = ServiceBrokerServiceProvider.createServiceBrokerClient(req, ServiceBrokerServiceProvider.CustomServiceBrokerServiceProviderProvisioningErrorHandler.class)

        ResponseEntity<CreateServiceInstanceResponse> re = makeCreateServiceInstanceCall(serviceBrokerClient, createServiceInstanceRequest, serviceInstance.guid)
        log.info("Async provisioning status of service instance with id ${serviceInstance.guid}: ${re.statusCode}")
        // If the service is OSB-spec compliant, HttpStatus.CREATED does not need to be supported
        return re.statusCode == HttpStatus.ACCEPTED || re.statusCode == HttpStatus.CREATED
    }

    // making the call to create a service instance via the serviceBrokerClient is defined in its own method so only this
    // method can be overwritten to enable testing of the ServiceBrokerServiceProvider in the TestableServiceBrokerServiceProviderClass
    // More details as to why this is necessary can be found in the TestableServiceBrokerServiceProvider class
    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, CreateServiceInstanceRequest createServiceInstanceRequest, String serviceInstanceId) {
        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(serviceInstanceId).withAsyncAccepted(true))
    }

    boolean deprovisionServiceInstance(ServiceInstance serviceInstance) {
        log.info("Async deprovisioning of service instance with id ${serviceInstance.guid}")
        GenericProvisionRequestPlanParameter req = ServiceBrokerServiceProvider.populateGenericProvisionRequestPlanParameter(serviceInstance.plan.parameters)
        ServiceBrokerClient serviceBrokerClient = ServiceBrokerServiceProvider.createServiceBrokerClient(req, ServiceBrokerServiceProvider.CustomServiceBrokerServiceProviderProvisioningErrorHandler.class)
        ResponseEntity<Void> re = makeDeleteServiceInstanceCall(serviceBrokerClient, serviceInstance.guid, req)

        // If the service is OSB-spec compliant, HttpStatus.OK does not need to be supported
        return re.statusCode == HttpStatus.ACCEPTED || re.statusCode == HttpStatus.OK
    }

    ResponseEntity<Void> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, String serviceInstanceId, GenericProvisionRequestPlanParameter req) {
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(serviceInstanceId, req.serviceId, req.planId, true)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }
}
