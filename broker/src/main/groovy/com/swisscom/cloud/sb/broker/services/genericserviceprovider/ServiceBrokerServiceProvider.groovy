package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

import static com.swisscom.cloud.sb.broker.services.common.Utils.verifyAsychronousCapableClient

@Component("ServiceBrokerServiceProvider")
@Slf4j
class ServiceBrokerServiceProvider implements ServiceProvider, AsyncServiceProvisioner {

    @Autowired
    AsyncProvisioningService asyncProvisioningService

    @Autowired
    ServiceBrokerServiceProviderConfig serviceBrokerServiceProviderConfig

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_INSTANCE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    private ServiceBrokerClient serviceBrokerClient

    ServiceBrokerServiceProvider() {}

    //@Autowired
    ServiceBrokerServiceProvider(ServiceBrokerClient serviceBrokerClient) {
        this.serviceBrokerClient = serviceBrokerClient
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        // else exception is thrown
        if(request.plan.asyncRequired) {
            verifyAsychronousCapableClient(request)
        }
        def params = request.plan.parameters
        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)

        // for testing purposes, a ServiceBrokerClient can be provided, if no ServiceBrokerClient is provided it has to be
        // initialized using the GenericProvisionrequestPlanParameter object.
        if(serviceBrokerClient == null) {
            serviceBrokerClient = createServiceBrokerClient(req)
        }

        def createServiceInstanceRequest = new CreateServiceInstanceRequest()
        //Check out ResponseEntity
        ResponseEntity<CreateServiceInstanceResponse> re = serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(request.serviceInstanceGuid).withAsyncAccepted(request.acceptsIncomplete))
        return new ProvisionResponse(isAsync: request.acceptsIncomplete)
    }

    //So far only sync
    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        def serviceInstanceId = request.serviceInstanceGuid
        def params = request.serviceInstance.plan.parameters

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        if(serviceBrokerClient == null) {
            serviceBrokerClient = createServiceBrokerClient(req)
        }
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(serviceInstanceId, req.serviceId, req.planId, request.acceptsIncomplete )
        serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)

        return new DeprovisionResponse(isAsync: request.acceptsIncomplete)
    }

    @Override
    BindResponse bind(BindRequest request) {
        def serviceInstanceId = request.serviceInstance.guid
        def serviceId = request.service.guid
        def planId = request.plan.guid
        def params = request.plan.parameters
        def bindingId = request.binding_guid

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        if(serviceBrokerClient == null) {
            serviceBrokerClient = createServiceBrokerClient(req)
        }
        CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest = new CreateServiceInstanceBindingRequest(serviceId, planId, null, null)
        createServiceInstanceBindingRequest.withBindingId(bindingId).withServiceInstanceId(serviceInstanceId)
        serviceBrokerClient.createServiceInstanceBinding(createServiceInstanceBindingRequest)

        return new BindResponse()
    }

    @Override
    void unbind(UnbindRequest request) {
        def serviceInstanceId = request.serviceInstance.guid
        def bindingId = request.binding.guid
        def serviceId = request.service.guid
        def planId = request.serviceInstance.plan.guid
        def params = request.serviceInstance.plan.parameters

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        if(serviceBrokerClient == null) {
            serviceBrokerClient = createServiceBrokerClient(req)
        }
        DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest = new DeleteServiceInstanceBindingRequest(serviceInstanceId, bindingId, serviceId, planId)
        serviceBrokerClient.deleteServiceInstanceBinding(deleteServiceInstanceBindingRequest)
    }

    GenericProvisionRequestPlanParameter populateGenericProvisionRequestPlanParameter(Set<Parameter> params) {
        GenericProvisionRequestPlanParameter req = new GenericProvisionRequestPlanParameter();
        Iterator<Parameter> it = params.iterator()
        while(it.hasNext()) {
            def next = it.next()
            switch(next.name) {
                case BASE_URL:
                    req.withBaseUrl(next.value)
                    break
                case USERNAME:
                    req.withUsername(next.value)
                    break
                case PASSWORD:
                    req.withPassword(next.value)
                    break
                case SERVICE_INSTANCE_ID:
                    req.withServiceId(next.value)
                    break
                case PLAN_ID:
                    req.withPlanId(next.value)
            }
        }
        return req
    }

    ServiceBrokerClient createServiceBrokerClient(GenericProvisionRequestPlanParameter req) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
        restTemplate.setErrorHandler(new CustomErrorHandler())
        return new ServiceBrokerClient(restTemplate, req.getBaseUrl(), req.getUsername(), req.getPassword())
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        return null
    }

    private class CustomErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // your error handling here
            if(response.statusCode == HttpStatus.NOT_FOUND){
                log.info("ServiceBrokerServiceProviderError: 404")
                throw new ServiceBrokerServiceProviderServiceNotFoundException("Service not found, response body:${response.body?.toString()}", null, null, HttpStatus.NOT_FOUND)
            } else if (response.statusCode == HttpStatus.BAD_REQUEST) {
                log.info("ServiceBrokerServiceProviderError: 400")
                throw new ServiceBrokerServiceProviderBadRequestException("Bad request, response body:${response.body?.toString()}", null, null, HttpStatus.BAD_REQUEST)
            } else if (response.statusCode == HttpStatus.CONFLICT) {
                log.info("ServiceBrokerServiceProviderError: 409")
                throw new ServiceBrokerException("Conflict, response body:${response.body?.toString()}", null, null, HttpStatus.CONFLICT)
            } else if (response.statusCode == HttpStatus.GONE) {
                log.info("ServiceBrokerServiceProviderError: 410")
                throw new ServiceBrokerException("Resource gone, response body:${response.body?.toString()}")
            } else if (response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                log.info("ServiceBrokerServiceProviderError: 422")
                throw new ServiceBrokerException("Unprocessable entity, response body:${response.body?.toString()}")
            }
            super.handleError(response)
        }
    }
}
