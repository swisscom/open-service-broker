package com.swisscom.cf.broker.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationResponseDto
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationStatusService
import com.swisscom.cf.broker.cfapi.dto.ProvisioningDto
import com.swisscom.cf.broker.error.ErrorCode
import com.swisscom.cf.broker.model.*
import com.swisscom.cf.broker.model.repository.CFServiceRepository
import com.swisscom.cf.broker.model.repository.PlanRepository
import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cf.broker.provisioning.ProvisioningService
import com.swisscom.cf.broker.services.common.DeprovisionResponse
import com.swisscom.cf.broker.services.common.ProvisionResponse
import com.swisscom.cf.broker.services.common.ProvisionResponseDto
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
class ProvisioningController extends BaseController {
    public static final String PARAM_ACCEPTS_INCOMPLETE = 'accepts_incomplete'

    @Autowired
    private ProvisioningService provisioningService
    @Autowired
    private ProvisioningPersistenceService provisioningPersistenceService
    @Autowired
    private LastOperationStatusService lastOperationStatusService
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private PlanRepository planRepository

    @RequestMapping(value = '/v2/service_instances/{instanceId}', method = RequestMethod.PUT)
    ResponseEntity<ProvisionResponseDto> provision(@PathVariable("instanceId") String serviceInstanceGuid,
                                                   @RequestParam(value = 'accepts_incomplete', required = false) boolean acceptsIncomplete,
                                                   @Valid @RequestBody ProvisioningDto provisioningDto) {

        log.info("Provision request for ServiceInstanceGuid:${serviceInstanceGuid}, ServiceId: ${provisioningDto?.service_id}, Params: ${provisioningDto.parameters}")

        failIfServiceInstaceAlreadyExists(serviceInstanceGuid)
        log.trace("ProvisioningDto:${provisioningDto.toString()}")

        ProvisionResponse provisionResponse = provisioningService.provision(createProvisionRequest(serviceInstanceGuid, provisioningDto, acceptsIncomplete))

        return new ResponseEntity<ProvisionResponseDto>(new ProvisionResponseDto(dashboard_url: provisionResponse.dashboardURL),
                provisionResponse.isAsync ? HttpStatus.ACCEPTED : HttpStatus.CREATED)
    }

    private ProvisionRequest createProvisionRequest(String serviceInstanceGuid, ProvisioningDto provisioning, boolean acceptsIncomple) {
        getAndCheckService(provisioning.service_id)

        ProvisionRequest provisionRequest = new ProvisionRequest()
        provisionRequest.serviceInstanceGuid = serviceInstanceGuid
        provisionRequest.organizationGuid = provisioning.organization_guid
        provisionRequest.spaceGuid = provisioning.space_guid
        provisionRequest.plan = getAndCheckPlan(provisioning.plan_id)
        provisionRequest.acceptsIncomplete = acceptsIncomple
        provisionRequest.parameters = serializeJson(provisioning.parameters)
        return provisionRequest
    }

    private String serializeJson(Map object) {
        if (!object) return null
        return new ObjectMapper().writeValueAsString(object)
    }

    private ServiceInstance failIfServiceInstaceAlreadyExists(String serviceInstanceGuid) {
        ServiceInstance instance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (instance) {
            log.debug "CFService instance with id ${instance.guid} already exists - returning 409 CONFLICT"
            ErrorCode.SERVICE_INSTANCE_ALREADY_EXISTS.throwNew()
        }
        return instance
    }

    private CFService getAndCheckService(String serviceGuid) {
        CFService cfService = cfServiceRepository.findByGuid(serviceGuid)
        if (!cfService) {
            log.debug("Service  with Guid:${serviceGuid} does not exist")
            ErrorCode.SERVICE_NOT_FOUND.throwNew("requested id:${serviceGuid}")
        }
        return cfService
    }

    private Plan getAndCheckPlan(String planGuid) {
        Plan plan = planRepository.findByGuid(planGuid)
        if (!plan) {
            log.debug("Plan  with Guid:${planGuid} does not exist")
            ErrorCode.PLAN_NOT_FOUND.throwNew("requested id:${planGuid}")
        }
        return plan
    }

    @RequestMapping(value = '/v2/service_instances/{instanceId}', method = RequestMethod.DELETE)
    ResponseEntity<String> deprovision(@PathVariable("instanceId") String serviceInstanceGuid,
                                       @RequestParam(value = "accepts_incomplete", required = false) boolean acceptsIncomplete) {
        log.info("Deprovision request for ServiceInstanceGuid: ${serviceInstanceGuid}")
        DeprovisionResponse response = provisioningService.deprovision(createDeprovisionRequest(serviceInstanceGuid, acceptsIncomplete))
        return new ResponseEntity<String>("{}", response.isAsync ? HttpStatus.ACCEPTED : HttpStatus.OK)
    }

    private DeprovisionRequest createDeprovisionRequest(String serviceInstanceGuid, boolean acceptsIncomplete) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!serviceInstance) {
            log.debug "Service instance with id: ${serviceInstanceGuid} does not exist - returning 410 GONE"
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }
        return new DeprovisionRequest(serviceInstanceGuid: serviceInstanceGuid, serviceInstance: serviceInstance, acceptsIncomplete: acceptsIncomplete)
    }

    @RequestMapping(value = "/v2/service_instances/{instanceId}/last_operation", method = RequestMethod.GET)
    LastOperationResponseDto lastOperation(@PathVariable("instanceId") String serviceInstanceGuid) {
        return lastOperationStatusService.pollJobStatus(serviceInstanceGuid)
    }
}