package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.sun.tools.javah.Gen
import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.BindResponse
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest
import groovy.util.logging.Slf4j
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component("ServiceBrokerServiceProvider")
@Slf4j
class ServiceBrokerServiceProvider implements ServiceProvider {

    private final String BASE_URL = "baseUrl"
    private final String USERNAME = "username"
    private final String PASSWORD = "password"
    private final String SERVICE_INSTANCE_ID = "service-guid"
    private final String PLAN_ID = "plan-guid"

    //So far only sync
    @Override
    ProvisionResponse provision(ProvisionRequest request) {
        def params = request.plan.parameters

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        ServiceBrokerClient sbc = createServiceBrokerClient(req)

        def createServiceInstanceRequest = new CreateServiceInstanceRequest()
        //Check out ResponseEntity
        ResponseEntity<CreateServiceInstanceResponse> re = sbc.createServiceInstance(createServiceInstanceRequest.withServiceInstanceId(request.serviceInstanceGuid).withAsyncAccepted(false))

        // || re.statusCode == 202 for async
        if (re.statusCode == 201) {
            return new ProvisionResponse(isAsync: false)
        } else {

        }
    }

    //So far only sync
    @Override
    DeprovisionResponse deprovision(DeprovisionRequest request) {
        def serviceInstanceId = request.serviceInstanceGuid
        def params = request.serviceInstance.plan.parameters

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        ServiceBrokerClient sbc = createServiceBrokerClient(req)
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(serviceInstanceId, req.serviceId, req.planId, false )
        sbc.deleteServiceInstance(deleteServiceInstanceRequest)

        return new DeprovisionResponse(isAsync: false)
    }

    @Override
    BindResponse bind(BindRequest request) {
        def serviceInstanceId = request.serviceInstance.guid
        def serviceId = request.service.guid
        def planId = request.plan.guid
        def params = request.plan.parameters
        def bindingId = request.binding_guid

        GenericProvisionRequestPlanParameter req = populateGenericProvisionRequestPlanParameter(params)
        ServiceBrokerClient sbc = createServiceBrokerClient(req)
        CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest = new CreateServiceInstanceBindingRequest(serviceId, planId, null, null)
        createServiceInstanceBindingRequest.withBindingId(bindingId).withServiceInstanceId(serviceInstanceId)
        sbc.createServiceInstanceBinding(createServiceInstanceBindingRequest)

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
        ServiceBrokerClient sbc = createServiceBrokerClient(req)
        DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest = new DeleteServiceInstanceBindingRequest(serviceInstanceId, bindingId, serviceId, planId)
        sbc.deleteServiceInstanceBinding(deleteServiceInstanceBindingRequest)
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
        return new ServiceBrokerClient(req.getBaseUrl(), req.getUsername(), req.getPassword())
    }
}
