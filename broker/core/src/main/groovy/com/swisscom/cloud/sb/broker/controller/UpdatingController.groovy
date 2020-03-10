/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.cfapi.dto.UpdateDto
import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.updating.UpdateResponseDto
import com.swisscom.cloud.sb.broker.updating.UpdatingService
import com.swisscom.cloud.sb.broker.util.Audit
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid
import java.security.Principal

@Api(value = "Service instance updating", description = "Endpoint for updating parameters and plan on a service instance.")
@RestController
@Slf4j
class UpdatingController extends BaseController {
    private ServiceInstanceRepository serviceInstanceRepository
    private PlanRepository planRepository
    private UpdatingService updatingService
    @Autowired
    private ServiceContextPersistenceService serviceContextService

    @Autowired
    UpdatingController(ServiceInstanceRepository serviceInstanceRepository, PlanRepository planRepository, UpdatingService updatingService) {
        this.serviceInstanceRepository = serviceInstanceRepository
        this.planRepository = planRepository
        this.updatingService = updatingService
    }

    @ApiOperation(value = "Updates an existing service instance.", response = UpdateResponseDto.class)
    @RequestMapping(value = '/v2/service_instances/{serviceInstanceGuid}', method = RequestMethod.PATCH)
    ResponseEntity<UpdateResponseDto> update(@PathVariable("serviceInstanceGuid") String serviceInstanceGuid,
                                             @RequestParam(value = 'accepts_incomplete', required = false) boolean acceptsIncomplete,
                                             @Valid @RequestBody UpdateDto updateDto,
                                             Principal principal) {
        def failed = false
        Map<String, Object> parametersForAudit = updateDto.getParameters()
        try{
            log.info("Update request for ServiceInstanceGuid:${serviceInstanceGuid}, ServiceId: ${updateDto?.service_id}, Params: ${updateDto?.parameters}")
            ServiceInstance serviceInstance = getServiceInstanceOrFail(serviceInstanceGuid)
            parametersForAudit = updatingService.getSanitizedSensitiveParameters(serviceInstance.getPlan(), updateDto?.getParameters())
            def updatingResponse = updatingService.update(
                    serviceInstance,
                    createUpdateRequest(serviceInstance, updateDto, acceptsIncomplete),
                    acceptsIncomplete)

            return new ResponseEntity<UpdateResponseDto>(new UpdateResponseDto(), updatingResponse.isAsync ? HttpStatus.ACCEPTED : HttpStatus.OK)
        } catch (Exception ex) {
            failed = true
            throw ex
        } finally {
            Audit.log("Update service instance",
                    [
                            serviceInstanceGuid: serviceInstanceGuid,
                            action: Audit.AuditAction.Update,
                            principal: principal.name,
                            async: acceptsIncomplete,
                            failed: failed,
                            parameters: parametersForAudit
                    ])
        }
    }

    private ServiceInstance getServiceInstanceOrFail(String serviceInstanceGuid) {
        ServiceInstance instance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!instance) {
            log.debug "Service instance with id ${serviceInstanceGuid} does not exist - returning 410 GONE"
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }
        return instance
    }

    private UpdateRequest createUpdateRequest(ServiceInstance serviceInstance, UpdateDto updateDto, boolean acceptsIncomplete) {
        Plan previousPlan = serviceInstance.plan
        Plan newPlan = serviceInstance.plan

        if (!StringUtils.isEmpty(updateDto.plan_id)) {
            newPlan = getAndCheckPlan(updateDto.plan_id)
            previousPlan = getAndCheckPreviousPlan(serviceInstance, updateDto)
        }

        return new UpdateRequest(
                serviceInstanceGuid: serviceInstance.guid,
                acceptsIncomplete: acceptsIncomplete,
                plan: newPlan,
                previousPlan: previousPlan,
                parameters: serializeJson(updateDto.parameters),
                serviceContext: serviceContextService.findOrCreate(updateDto.context, serviceInstance.guid))
    }

    private static String serializeJson(Map object) {
        return object ? new ObjectMapper().writeValueAsString(object) : null
    }

    private Plan getAndCheckPreviousPlan(ServiceInstance serviceInstance, UpdateDto updateDto) {
        Plan previousPlan = serviceInstance.plan

        if (updateDto.previous_values != null && !StringUtils.isEmpty(updateDto.previous_values.plan_id)) {
            previousPlan = getAndCheckPlan(updateDto.previous_values.plan_id)

            if (previousPlan.guid != serviceInstance.plan.guid) {
                ErrorCode.UPDATE_INCORRECT_PLAN_ID.throwNew()
            }
        }

        return previousPlan
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
