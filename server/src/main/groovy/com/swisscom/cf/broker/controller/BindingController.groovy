package com.swisscom.cf.broker.controller

import com.swisscom.cf.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cf.broker.cfapi.dto.BindRequestDto
import com.swisscom.cf.broker.cfapi.dto.UnbindingDto
import com.swisscom.cf.broker.exception.ErrorCode
import com.swisscom.cf.broker.model.CFService
import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.ServiceBinding
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.model.repository.CFServiceRepository
import com.swisscom.cf.broker.model.repository.PlanRepository
import com.swisscom.cf.broker.model.repository.ServiceBindingRepository
import com.swisscom.cf.broker.services.common.BindRequest
import com.swisscom.cf.broker.services.common.BindResponse
import com.swisscom.cf.broker.services.common.UnbindRequest
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@CompileStatic
@Log4j
class BindingController extends BaseController {
    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService
    @Autowired
    private ServiceBindingRepository serviceBindingRepository
    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private PlanRepository planRepository

    @RequestMapping(value = '/v2/service_instances/{service_instance}/service_bindings/{id}', method = RequestMethod.PUT)
    ResponseEntity<String> bind(@PathVariable('id') String bindingId,
                                @PathVariable('service_instance') String serviceInstanceId,
                                @Valid @RequestBody BindRequestDto bindingDto) {
        log.info("Bind request for bindingId: ${bindingId}, serviceId: ${bindingDto?.service_id} and serviceInstanceGuid: ${serviceInstanceId}")

        ServiceInstance serviceInstance = getAndCheckServiceInstance(serviceInstanceId)
        CFService service = getAndCheckService(bindingDto)
        failIfServiceBindingAlreadyExists(bindingId)

        BindResponse bindResponse = findServiceProvider(serviceInstance.plan).bind(createBindRequest(bindingDto, service, serviceInstance))

        serviceBindingPersistenceService.create(serviceInstance, getCredentialsAsJson(bindResponse), bindingId, bindResponse.details)

        return new ResponseEntity<String>(getCredentialsAsJson(bindResponse), bindResponse.isUniqueCredentials ? HttpStatus.CREATED : HttpStatus.OK)
    }

    private static String getCredentialsAsJson(BindResponse bindResponse) {
        return bindResponse.credentials ? bindResponse.credentials.toJson() : ""
    }

    private void failIfServiceBindingAlreadyExists(String bindingId) {
        ServiceBinding serviceBinding = serviceBindingRepository.findByGuid(bindingId)
        if (serviceBinding) {
            ErrorCode.SERVICE_BINDING_ALREADY_EXISTS.throwNew()
        }
    }

    private CFService getAndCheckService(BindRequestDto bindingDto) {
        CFService service = cfServiceRepository.findByGuid(bindingDto.service_id)
        if (!service) {
            ErrorCode.SERVICE_NOT_FOUND.throwNew("no service with id:${bindingDto.service_id} found")
        }
        //TODO check if service is bindable
        return service
    }

    private Plan getAndCheckPlan(BindRequestDto bindingDto) {
        Plan plan = planRepository.findByGuid(bindingDto.plan_id)
        if (!plan) {
            ErrorCode.PLAN_NOT_FOUND.throwNew("no plan with id:${bindingDto.service_id} found")
        }
        return plan
    }

    private BindRequest createBindRequest(BindRequestDto bindingDto, CFService service, ServiceInstance serviceInstance) {
        BindRequest bindRequest = new BindRequest()
        bindRequest.app_guid = bindingDto.app_guid
        bindRequest.serviceInstance = serviceInstance
        bindRequest.service = service
        bindRequest.plan = getAndCheckPlan(bindingDto)
        bindRequest.parameters = bindingDto.parameters
        return bindRequest
    }

    @RequestMapping(value = '/v2/service_instances/{service_instance}/service_bindings/{id}', method = RequestMethod.DELETE)
    def unbind(@PathVariable('service_instance') String serviceInstanceId,
               @PathVariable('id') String bindingGuid,
               UnbindingDto unbindingDto) {
        log.info("Unbind request for BindingId: ${bindingGuid} and ServiceInstanceid: ${serviceInstanceId}")
        ServiceBinding serviceBinding = checkServiceBinding(bindingGuid)
        ServiceInstance serviceInstance = serviceBinding.serviceInstance
        CFService service = serviceInstance.plan.service


        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, service: service, serviceInstance: serviceInstance)
        findServiceProvider(serviceInstance.plan).unbind(unbindRequest)

        serviceBindingPersistenceService.delete(serviceBinding, serviceInstance)
        log.info("Servicebinding ${serviceBinding.guid} deleted")
        return void
    }

    private ServiceBinding checkServiceBinding(String bindingGuid) {
        ServiceBinding serviceBinding = serviceBindingRepository.findByGuid(bindingGuid)
        if (!serviceBinding) {
            ErrorCode.SERVICE_BINDING_NOT_FOUND.throwNew()
        }
        return serviceBinding
    }
}
