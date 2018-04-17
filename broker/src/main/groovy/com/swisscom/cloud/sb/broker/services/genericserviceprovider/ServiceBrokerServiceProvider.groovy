package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.google.common.annotations.VisibleForTesting
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.async.AsyncProvisioningService
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointLookup
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncOperationResult
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceDeprovisioner
import com.swisscom.cloud.sb.broker.provisioning.async.AsyncServiceProvisioner
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceStateWithAction
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachine
import com.swisscom.cloud.sb.broker.services.AsyncServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider

import com.swisscom.cloud.sb.broker.services.genericserviceprovider.client.ServiceBrokerServiceProviderFacade
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.client.ServiceBrokerServiceProviderRestClient
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.config.ServiceBrokerServiceProviderConfig
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine.ServiceBrokerServiceProviderDeprovisionState
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine.ServiceBrokerServiceProviderProvisionState
import com.swisscom.cloud.sb.broker.services.genericserviceprovider.statemachine.ServiceBrokerServiceProviderStateMachineContext
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Component("ServiceBrokerServiceProvider")
@Slf4j
class ServiceBrokerServiceProvider extends AsyncServiceProvider<ServiceBrokerServiceProviderConfig> implements ServiceProvider, AsyncServiceProvisioner, AsyncServiceDeprovisioner, ServiceUsageProvider {

    @Autowired
    ServiceBrokerServiceProviderFacade sbspFacade

    @Autowired
    ServiceBrokerServiceProviderRestClient sbspRestClient

    @Autowired
    ServiceBrokerServiceProviderUsage serviceBrokerServiceProviderUsage

    private final static String BASE_URL = "baseUrl"
    private final static String USERNAME = "username"
    private final static String PASSWORD = "password"
    private final static String SERVICE_INSTANCE_ID = "service-guid"
    private final static String PLAN_ID = "plan-guid"

    protected ServiceBrokerClient serviceBrokerClient

    ServiceBrokerServiceProvider() {}


    ServiceBrokerServiceProvider(ServiceBrokerClient serviceBrokerClient) {
        this.serviceBrokerClient = serviceBrokerClient
    }

    ServiceBrokerServiceProvider(ServiceBrokerServiceProviderUsage serviceBrokerServiceProviderUsage) {
        this.serviceBrokerServiceProviderUsage = serviceBrokerServiceProviderUsage
    }

    ServiceBrokerServiceProvider(ServiceBrokerClient serviceBrokerClient, ServiceBrokerServiceProviderUsage serviceBrokerServiceProviderUsage) {
        this.serviceBrokerClient = serviceBrokerClient
        this.serviceBrokerServiceProviderUsage = serviceBrokerServiceProviderUsage
    }

    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        if (request.plan.asyncRequired) {
            super.provision(request)
        } else {

            def params = request.plan.parameters
            GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)

            // for testing purposes, a ServiceBrokerClient can be provided, if no ServiceBrokerClient is provided it has to be
            // initialized using the GenericProvisionRequestPlanParameter object.
            if (serviceBrokerClient == null) {
                serviceBrokerClient = createServiceBrokerClient(req, CustomServiceBrokerServiceProviderProvisioningErrorHandler.class)
            }

            def createServiceInstanceRequest = new CreateServiceInstanceRequest(req.serviceId, req.planId, null, null, null)
            //Check out ResponseEntity
            ResponseEntity<CreateServiceInstanceResponse> re = makeCreateServiceInstanceCall(createServiceInstanceRequest, request)
            return new ProvisionResponse(isAsync: request.plan.asyncRequired)
        }
    }

    // making the call to create a service instance via the serviceBrokerClient is defined in its own method so only this
    // method can be overwritten to enable testing of the ServiceBrokerServiceProvider in the TestableServiceBrokerServiceProviderClass
    // More details as to why this is necessary can be found in the TestableServiceBrokerServiceProvider class
    ResponseEntity<CreateServiceInstanceResponse> makeCreateServiceInstanceCall(CreateServiceInstanceRequest createServiceInstanceRequest, ProvisionRequest request) {
        return serviceBrokerClient.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(request.serviceInstanceGuid).withAsyncAccepted(request.acceptsIncomplete))
    }

    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        if (request.serviceInstance.plan.asyncRequired) {
            super.deprovision(request)
        } else {
            def params = request.serviceInstance.plan.parameters

            GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
            if (serviceBrokerClient == null) {
                serviceBrokerClient = createServiceBrokerClient(req, CustomServiceBrokerServiceProviderDeprovisioningErrorHandler.class)
            }

            ResponseEntity<DeleteServiceInstanceResponse> re = makeDeleteServiceInstanceCall(serviceBrokerClient, request, req)
            return new DeprovisionResponse(isAsync: request.serviceInstance.plan.asyncRequired)
        }
    }

    ResponseEntity<DeleteServiceInstanceResponse> makeDeleteServiceInstanceCall(ServiceBrokerClient serviceBrokerClient, DeprovisionRequest request, GenericProvisionRequestPlanParameter req) {
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(request.serviceInstanceGuid, req.serviceId, req.planId, request.acceptsIncomplete)
        return serviceBrokerClient.deleteServiceInstance(deleteServiceInstanceRequest)
    }

    @Override
    BindResponse bind(BindRequest request) {
        def serviceInstanceId = request.serviceInstance.guid
        def serviceId = request.service.guid
        def planId = request.plan.guid
        def params = request.plan.parameters
        def bindingId = request.binding_guid

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        if (serviceBrokerClient == null) {
            serviceBrokerClient = createServiceBrokerClient(req, CustomServiceBrokerServiceProviderBindingErrorHandler)
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
        if (serviceBrokerClient == null) {
            serviceBrokerClient = createServiceBrokerClient(req, CustomServiceBrokerServiceProviderUnbindingErrorHandler.class)
        }
        DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest = new DeleteServiceInstanceBindingRequest(serviceInstanceId, bindingId, serviceId, planId)
        serviceBrokerClient.deleteServiceInstanceBinding(deleteServiceInstanceBindingRequest)
    }

    @Override
    UpdateResponse update(UpdateRequest request) {
        return null
    }

    public static GenericProvisionRequestPlanParameter populateGenericProvisionRequestPlanParameter(Set<Parameter> params) {
        GenericProvisionRequestPlanParameter req = new GenericProvisionRequestPlanParameter();
        Iterator<Parameter> it = params.iterator()
        while (it.hasNext()) {
            def next = it.next()
            switch (next.name) {
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

    public static ServiceBrokerClient createServiceBrokerClient(GenericProvisionRequestPlanParameter req, Class errorHandler) {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
        restTemplate.setErrorHandler(errorHandler.newInstance())
        return new ServiceBrokerClient(restTemplate, req.getBaseUrl(), req.getUsername(), req.getPassword())
    }

    @Override
    AsyncOperationResult requestProvision(LastOperationJobContext context) {
        StateMachine stateMachine = createProvisionStateMachine()
        ServiceStateWithAction currentState = getProvisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
        AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState)
    }

    @VisibleForTesting
    private StateMachine createProvisionStateMachine() {
        new StateMachine([ServiceBrokerServiceProviderProvisionState.PROVISION_IN_PROGRESS,
                          ServiceBrokerServiceProviderProvisionState.PROVISION_SUCCESS,
                          ServiceBrokerServiceProviderProvisionState.PROVISION_FAILED])
    }

    @VisibleForTesting
    private ServiceStateWithAction getProvisionState(LastOperationJobContext context) {
        ServiceStateWithAction provisionState = null
        if (!context.lastOperation.internalState) {
            provisionState = ServiceBrokerServiceProviderProvisionState.PROVISION_IN_PROGRESS
        } else {
            provisionState = ServiceBrokerServiceProviderProvisionState.of(context.lastOperation.internalState)
        }
        return provisionState
    }

    @VisibleForTesting
    private ServiceBrokerServiceProviderStateMachineContext createStateMachineContext(LastOperationJobContext context) {
        return new ServiceBrokerServiceProviderStateMachineContext(lastOperationJobContext: context, sbspFacade: sbspFacade, sbspRestClient: sbspRestClient)
    }

    @Override
    Optional<AsyncOperationResult> requestDeprovision(LastOperationJobContext context) {
        StateMachine stateMachine = createDeprovisionStateMachine()
        ServiceStateWithAction currentState = getDeprovisionState(context)
        def actionResult = stateMachine.setCurrentState(currentState, createStateMachineContext(context))
        Optional.of(AsyncOperationResult.of(actionResult.go2NextState ? stateMachine.nextState(currentState) : currentState))
    }

    @VisibleForTesting
    private StateMachine createDeprovisionStateMachine() {
        new StateMachine([ServiceBrokerServiceProviderDeprovisionState.DEPROVISION_IN_PROGRESS,
                          ServiceBrokerServiceProviderDeprovisionState.DEPROVISION_SUCCESS,
                          ServiceBrokerServiceProviderDeprovisionState.DEPROVISION_FAILED])
    }

    @VisibleForTesting
    private ServiceStateWithAction getDeprovisionState(LastOperationJobContext context) {
        ServiceStateWithAction deprovisionState = null
        if (!context.lastOperation.internalState) {
            deprovisionState = ServiceBrokerServiceProviderDeprovisionState.DEPROVISION_IN_PROGRESS
        } else {
            deprovisionState = ServiceBrokerServiceProviderDeprovisionState.of(context.lastOperation.internalState)
        }
        return deprovisionState
    }

    @Override
    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        return serviceBrokerServiceProviderUsage.findUsage(serviceInstance, enddate)
    }

    @Override
    Collection<Extension> buildExtensions() {
        return [new Extension("discovery_url": "discoveryURL")]
    }

    public class CustomServiceBrokerServiceProviderProvisioningErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.statusCode == HttpStatus.BAD_REQUEST) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_PROVISIONING_BAD_REQUEST.throwNew()
            } else if (response.statusCode == HttpStatus.CONFLICT) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_PROVISIONING_CONFLICT.throwNew()
            } else if (response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_PROVISIONING_UNPROCESSABLE_ENTITY.throwNew()
            } else {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_INTERNAL_SERVER_ERROR.throwNew()
            }
            super.handleError(response)
        }
    }

    private class CustomServiceBrokerServiceProviderBindingErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.statusCode == HttpStatus.BAD_REQUEST) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_BAD_REQUEST.throwNew()
            } else if (response.statusCode == HttpStatus.CONFLICT) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_CONFLICT.throwNew()
            } else if (response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_BINDING_UNPROCESSABLE_ENTITY.throwNew()
            } else {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_INTERNAL_SERVER_ERROR.throwNew()
            }
            super.handleError(response)
        }
    }

    private class CustomServiceBrokerServiceProviderUnbindingErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.statusCode == HttpStatus.BAD_REQUEST) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_UNBINDING_BAD_REQUEST.throwNew()
            } else if (response.statusCode == HttpStatus.GONE) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_UNBINDING_GONE.throwNew()
            } else {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_INTERNAL_SERVER_ERROR.throwNew()
            }
        }
    }

    private class CustomServiceBrokerServiceProviderDeprovisioningErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.statusCode == HttpStatus.BAD_REQUEST) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_DEPROVISIONING_BAD_REQUEST.throwNew()
            } else if (response.statusCode == HttpStatus.GONE) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_DEPROVISIONING_GONE.throwNew()
            } else if (response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_DEPROVISIONING_UNPROCESSABLE_ENTITY.throwNew()
            } else {
                ErrorCode.SERVICEBROKERSERVICEPROVIDER_INTERNAL_SERVER_ERROR.throwNew()
            }
        }
    }
}
