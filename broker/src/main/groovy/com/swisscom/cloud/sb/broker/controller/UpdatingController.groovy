package com.swisscom.cloud.sb.broker.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.cfapi.dto.UpdateDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.updating.UpdateResponseDto
import com.swisscom.cloud.sb.broker.updating.UpdatingService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@Api(value = "Service instance updating", description = "Endpoint for updating parameters and plan on a service instance.")
@RestController
class UpdatingController extends BaseController {
    private ServiceInstanceRepository serviceInstanceRepository
    private UpdatingService updatingService
    PlanRepository planRepository

    @Autowired
    UpdatingController(ServiceInstanceRepository serviceInstanceRepository, UpdatingService updatingService, PlanRepository planRepository)
    {
        this.serviceInstanceRepository = serviceInstanceRepository
        this.updatingService = updatingService
        this.planRepository = planRepository
    }

    @ApiOperation(value = "Updates an existing service instance.", response = UpdateResponseDto.class)
    @RequestMapping(value = '/v2/service_instances/{instanceId}', method = RequestMethod.PATCH)
    ResponseEntity<UpdateResponseDto> update(@PathVariable("instanceId") String serviceInstanceGuid,
                                             @RequestParam(value = 'accepts_incomplete', required = false) boolean acceptsIncomplete,
                                             @Valid @RequestBody UpdateDto updateDto) {
        log.info("Provision request for ServiceInstanceGuid:${serviceInstanceGuid}, ServiceId: ${updateDto?.service_id}, Params: ${updateDto.parameters}")
        ServiceInstance serviceInstance = getServiceInstanceOrFail(serviceInstanceGuid)

        def updatingResponse = updatingService.Update(
                serviceInstance,
                createUpdateRequest(serviceInstance.guid, updateDto, acceptsIncomplete),
                acceptsIncomplete)

        return new ResponseEntity<UpdateResponseDto>(new UpdateResponseDto(), updatingResponse.isAsync ? HttpStatus.ACCEPTED : HttpStatus.CREATED)
    }

    private ServiceInstance getServiceInstanceOrFail(String serviceInstanceGuid) {
        ServiceInstance instance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!instance) {
            log.debug "Service instance with id ${instance.guid} does not exist - returning 410 GONE"
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }
        return instance
    }

    private UpdateRequest createUpdateRequest(String serviceInstanceGuid, UpdateDto updateDto, boolean acceptsIncomplete) {
        return new UpdateRequest(
                serviceInstanceGuid: serviceInstanceGuid,
                acceptsIncomplete: acceptsIncomplete,
                plan: getAndCheckPlan(updateDto.plan_id),
                previousPlan: getAndCheckPlan(updateDto.previous_values.plan_id),
                parameters: serializeJson(updateDto.parameters))
    }

    private static String serializeJson(Map object) {
        return object ? new ObjectMapper().writeValueAsString(object) : null
    }

    private Plan getAndCheckPlan(String planGuid) {
        Plan plan = planRepository.findByGuid(planGuid)
        if (!plan) {
            log.debug("Plan  with Guid:${planGuid} does not exist")
            ErrorCode.PLAN_NOT_FOUND.throwNew("requested id:${planGuid}")
        }

        return plan
    }
}
