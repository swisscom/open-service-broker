package com.swisscom.cloud.sb.broker.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.binding.*
import com.swisscom.cloud.sb.broker.cfapi.converter.ServiceInstanceBindingDtoConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.BindRequestDto
import com.swisscom.cloud.sb.broker.cfapi.dto.UnbindingDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.metrics.BindingMetricsService
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid
import java.security.Principal

@Api(value = "binding", description = "Endpoint for service bindings")
@RestController
@CompileStatic
@Slf4j
class BindingController extends BaseController {
    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService
    @Autowired
    private ServiceBindingRepository serviceBindingRepository
    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private PlanRepository planRepository
    @Autowired
    private ServiceInstanceBindingDtoConverter bindingDtoConverter

    @Autowired
    private BindingMetricsService bindingMetricsService

    @ApiOperation(value = "Bind service")
    @RequestMapping(value = '/v2/service_instances/{service_instance}/service_bindings/{id}', method = RequestMethod.PUT)
    ResponseEntity<String> bind(@PathVariable('id') String bindingId,
                                @PathVariable('service_instance') String serviceInstanceId,
                                @Valid @RequestBody BindRequestDto bindingDto,
                                Principal principal) {
        log.info("Bind request for bindingId: ${bindingId}, serviceId: ${bindingDto?.service_id} and serviceInstanceGuid: ${serviceInstanceId}")

        ServiceInstance serviceInstance = getAndCheckServiceInstance(serviceInstanceId)
        bindingMetricsService.setTotalBindingRequestsPerService(serviceInstance)
        verifyServiceInstanceIsReady(serviceInstance) //Don't allow binding if service is not ready
        CFService service = getAndCheckService(bindingDto)
        failIfServiceBindingAlreadyExists(bindingId)

        BindResponse bindResponse = findServiceProvider(serviceInstance.plan).bind(createBindRequest(bindingId, bindingDto, service, serviceInstance))

        serviceBindingPersistenceService.create(serviceInstance, getCredentialsAsJson(bindResponse), serializeJson(bindingDto.parameters), bindingId, bindResponse.details, bindingDto.context, principal.name)

        bindingMetricsService.setSuccessfulBindingRequestsPerService(serviceInstance)
        return new ResponseEntity<String>(getCredentialsAsJson(bindResponse), bindResponse.isUniqueCredentials ? HttpStatus.CREATED : HttpStatus.OK)
    }

    private static String getCredentialsAsJson(BindResponse bindResponse) {
        return bindResponse.credentials ? bindResponse.credentials.toJson() : ""
    }

    private static String serializeJson(Object object) {
        if (!object) return null
        return new ObjectMapper().writeValueAsString(object)
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

        if (!service.bindable) {
            ErrorCode.SERVICE_NOT_BINDABLE.throwNew("the service with the id:${bindingDto.service_id} is not bindable")
        }
        return service
    }

    private Plan getAndCheckPlan(BindRequestDto bindingDto) {
        Plan plan = planRepository.findByGuid(bindingDto.plan_id)
        if (!plan) {
            ErrorCode.PLAN_NOT_FOUND.throwNew("no plan with id:${bindingDto.service_id} found")
        }
        return plan
    }

    private BindRequest createBindRequest(String bindingId, BindRequestDto bindingDto, CFService service, ServiceInstance serviceInstance) {
        BindRequest bindRequest = new BindRequest()
        bindRequest.binding_guid = bindingId
        bindRequest.app_guid = bindingDto.app_guid
        bindRequest.serviceInstance = serviceInstance
        bindRequest.service = service
        bindRequest.plan = getAndCheckPlan(bindingDto)
        bindRequest.parameters = bindingDto.parameters
        return bindRequest
    }

    @ApiOperation(value = "Delete service binding")
    @RequestMapping(value = '/v2/service_instances/{service_instance}/service_bindings/{id}', method = RequestMethod.DELETE)
    def unbind(@PathVariable('service_instance') String serviceInstanceId,
               @PathVariable('id') String bindingGuid,
               UnbindingDto unbindingDto) {
        log.info("Unbind request for BindingId: ${bindingGuid} and ServiceInstanceid: ${serviceInstanceId}")
        ServiceBinding serviceBinding
        try {
            serviceBinding = checkServiceBinding(bindingGuid)
        } catch (ServiceBrokerException e) {
            if (e.code == ErrorCode.SERVICE_BINDING_NOT_FOUND.code) ErrorCode.SERVICE_BINDING_GONE.throwNew()
            else throw e
        }
        ServiceInstance serviceInstance = serviceBinding.serviceInstance
        CFService service = serviceInstance.plan.service


        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, service: service, serviceInstance: serviceInstance)
        findServiceProvider(serviceInstance.plan).unbind(unbindRequest)

        serviceBindingPersistenceService.delete(serviceBinding, serviceInstance)
        log.info("Servicebinding ${serviceBinding.guid} deleted")
        return new ResponseEntity<String>('{}', HttpStatus.OK)
    }

    @ApiOperation(value = "Fetch service instance's binding", response = ServiceInstanceBindingResponseDto.class)
    @RequestMapping(value = "/v2/service_instances/{instanceId}/service_bindings/{bindingId}", method = RequestMethod.GET)
    ServiceInstanceBindingResponseDto getServiceInstanceBinding(@PathVariable("instanceId") String serviceInstanceGuid,
                                                                @PathVariable("bindingId") String bindingGuid) {
        checkServiceBinding(bindingGuid)
        def serviceBinding = serviceBindingRepository.findByGuid(bindingGuid)
        if (!serviceBinding.serviceInstance.plan.service.bindingsRetrievable) {
            ErrorCode.SERVICE_BINDING_NOT_RETRIEVABLE.throwNew()
        }
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceBinding.serviceInstance.plan)
        if (!(serviceProvider instanceof FetchServiceBindingProvider)) {
            return bindingDtoConverter.convert(serviceBinding)
        } else {
            FetchServiceBindingProvider provider = serviceProvider as FetchServiceBindingProvider
            return provider.fetchServiceBinding(serviceBinding)
        }
    }

    private ServiceBinding checkServiceBinding(String bindingGuid) {
        ServiceBinding serviceBinding = serviceBindingRepository.findByGuid(bindingGuid)
        if (!serviceBinding) {
            ErrorCode.SERVICE_BINDING_NOT_FOUND.throwNew()
        }
        return serviceBinding
    }
}
