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
import com.swisscom.cloud.sb.broker.cfapi.converter.ServiceInstanceDtoConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.ProvisioningDto
import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.provisioning.DeprovisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponse
import com.swisscom.cloud.sb.broker.provisioning.ProvisionResponseDto
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningService
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationResponseDto
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationStatusService
import com.swisscom.cloud.sb.broker.provisioning.serviceinstance.FetchServiceInstanceProvider
import com.swisscom.cloud.sb.broker.provisioning.serviceinstance.ServiceInstanceResponseDto
import com.swisscom.cloud.sb.broker.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.repository.PlanRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.util.Audit
import com.swisscom.cloud.sb.broker.util.SensitiveParameterProvider
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.servicebroker.model.CloudFoundryContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.validation.Valid
import java.security.Principal

@Api(value = "Service provisioning", description = "Endpoint for provisioning/deprovisoning")
@RestController
@CompileStatic
class ProvisioningController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisioningController.class)

    private ProvisioningService provisioningService
    private LastOperationStatusService lastOperationStatusService
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceContextPersistenceService serviceContextService
    private CFServiceRepository cfServiceRepository
    private PlanRepository planRepository
    private ServiceProviderLookup serviceProviderLookup
    private ControllerHelper controllerHelper

    ProvisioningController(ProvisioningService provisioningService,
                           LastOperationStatusService lastOperationStatusService,
                           ServiceInstanceRepository serviceInstanceRepository,
                           ServiceContextPersistenceService serviceContextService,
                           CFServiceRepository cfServiceRepository,
                           PlanRepository planRepository,
                           ServiceProviderLookup serviceProviderLookup) {
        this.controllerHelper = new ControllerHelper(serviceInstanceRepository)
        this.provisioningService = provisioningService
        this.lastOperationStatusService = lastOperationStatusService
        this.serviceInstanceRepository = serviceInstanceRepository
        this.serviceContextService = serviceContextService
        this.cfServiceRepository = cfServiceRepository
        this.planRepository = planRepository
        this.serviceProviderLookup = serviceProviderLookup
    }

    @ApiOperation(value = "Provision a new service instance", response = ProvisionResponseDto.class)
    @RequestMapping(value = '/v2/service_instances/{serviceInstanceGuid}', method = RequestMethod.PUT)
    ResponseEntity<ProvisionResponseDto> provision(@PathVariable("serviceInstanceGuid") String serviceInstanceGuid,
                                                   @RequestParam(value = 'accepts_incomplete', required = false) boolean acceptsIncomplete,
                                                   @Valid @RequestBody ProvisioningDto provisioningDto,
                                                   Principal principal) {
        def failed = false
        def hasSensitiveData = false

        try {
            failIfServiceInstanceAlreadyExists(serviceInstanceGuid)

            def serviceProvider = serviceProviderLookup.findServiceProvider(getAndCheckPlan(provisioningDto.plan_id))
            if (serviceProvider instanceof SensitiveParameterProvider) {
                LOGGER.
                        info("Provision request for ServiceInstanceGuid: {}, ServiceId: {}",
                             serviceInstanceGuid,
                             provisioningDto?.service_id)
                hasSensitiveData = true
            } else {
                LOGGER.
                        info("Provision request for ServiceInstanceGuid: {}, ServiceId: {}, Params: {}",
                             serviceInstanceGuid,
                             provisioningDto?.service_id,
                             provisioningDto.parameters)
                LOGGER.trace("ProvisioningDto: {}", provisioningDto.toString())
            }

            def request = createProvisionRequest(serviceInstanceGuid, provisioningDto, acceptsIncomplete, principal)

            ProvisionResponse provisionResponse = provisioningService.provision(request)

            if (provisionResponse.extensions) {
                return new ResponseEntity<ProvisionResponseDto>(new ProvisionResponseDto(dashboard_url:
                                                                                                 provisionResponse.
                                                                                                         dashboardURL,
                                                                                         extension_apis:
                                                                                                 provisionResponse.
                                                                                                         extensions),
                                                                provisionResponse.isAsync ? HttpStatus.ACCEPTED :
                                                                HttpStatus.CREATED)
            } else {
                return new ResponseEntity<ProvisionResponseDto>(new ProvisionResponseDto(dashboard_url:
                                                                                                 provisionResponse.
                                                                                                         dashboardURL),
                                                                provisionResponse.isAsync ? HttpStatus.ACCEPTED :
                                                                HttpStatus.CREATED)
            }
        } catch (Exception ex) {
            failed = true
            throw ex
        } finally {
            Audit.log("Provision new service instance",
                      [
                              serviceInstanceGuid: serviceInstanceGuid,
                              action             : Audit.AuditAction.Provision,
                              principal          : principal.name,
                              async              : acceptsIncomplete,
                              parameters         : hasSensitiveData ? null : provisioningDto.parameters,
                              failed             : failed
                      ])
        }
    }

    private handleCloudFoundryLegacyContext(ProvisioningDto provisioningDto) {
        if (!provisioningDto.context && (provisioningDto.organization_guid && provisioningDto.space_guid)) {
            provisioningDto.context = CloudFoundryContext.builder()
                                                         .organizationGuid(provisioningDto.organization_guid)
                                                         .spaceGuid(provisioningDto.space_guid)
                                                         .build()
        }
    }

    private ProvisionRequest createProvisionRequest(String serviceInstanceGuid,
                                                    ProvisioningDto provisioning,
                                                    boolean acceptsIncomplete,
                                                    Principal principal) {
        getAndCheckService(provisioning.service_id)

        ProvisionRequest provisionRequest = new ProvisionRequest()
        provisionRequest.serviceInstanceGuid = serviceInstanceGuid
        provisionRequest.plan = getAndCheckPlan(provisioning.plan_id)
        provisionRequest.acceptsIncomplete = acceptsIncomplete
        provisionRequest.parameters = serializeJson(provisioning.parameters)
        provisionRequest.applicationUser = principal.name

        handleCloudFoundryLegacyContext(provisioning)
        provisionRequest.serviceContext = serviceContextService.findOrCreate(provisioning.context, serviceInstanceGuid)

        return provisionRequest
    }

    private static String serializeJson(Object object) {
        if (!object) {
            return null
        }
        return new ObjectMapper().writeValueAsString(object)
    }

    private ServiceInstance failIfServiceInstanceAlreadyExists(String serviceInstanceGuid) {
        ServiceInstance instance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (instance) {
            LOGGER.debug("CFService instance with id {} already exists - returning 409 CONFLICT", instance.guid)
            ErrorCode.SERVICE_INSTANCE_ALREADY_EXISTS.throwNew()
        }
        return instance
    }

    private CFService getAndCheckService(String serviceGuid) {
        CFService cfService = cfServiceRepository.findByGuid(serviceGuid)
        if (!cfService) {
            LOGGER.debug("Service  with Guid: {} does not exist", serviceGuid)
            ErrorCode.SERVICE_NOT_FOUND.throwNew("requested id: " + serviceGuid)
        } else if (!cfService.active) {
            LOGGER.debug("Service with Guid: {} is not active", serviceGuid)
            ErrorCode.SERVICE_NOT_ACTIVE.throwNew("requested id: " + serviceGuid)
        }
        return cfService
    }

    private Plan getAndCheckPlan(String planGuid) {
        Plan plan = planRepository.findByGuid(planGuid)
        if (!plan) {
            LOGGER.debug("Plan  with Guid: {} does not exist", planGuid)
            ErrorCode.PLAN_NOT_FOUND.throwNew("requested id: " + planGuid)
        } else if (!plan.active) {
            LOGGER.debug("Plan with Guid: {} is not active", planGuid)
            ErrorCode.PLAN_NOT_ACTIVE.throwNew("requested id: " + planGuid)
        }
        return plan
    }

    @ApiOperation(value = "Deprovision a service instance")
    @RequestMapping(value = '/v2/service_instances/{serviceInstanceGuid}', method = RequestMethod.DELETE)
    ResponseEntity<String> deprovision(@PathVariable("serviceInstanceGuid") String serviceInstanceGuid,
                                       @RequestParam(value = "accepts_incomplete", required = false) boolean acceptsIncomplete,
                                       Principal principal) {
        def failed = false
        try {
            LOGGER.info("Deprovision request for ServiceInstanceGuid: {}", serviceInstanceGuid)
            DeprovisionResponse response = provisioningService.
                    deprovision(createDeprovisionRequest(serviceInstanceGuid, acceptsIncomplete))
            return new ResponseEntity<String>("{}", response.isAsync ? HttpStatus.ACCEPTED : HttpStatus.OK)
        } catch (Exception ex) {
            failed = true
            throw ex
        } finally {
            Audit.log("Deprovision service instance",
                      [
                              serviceInstanceGuid: serviceInstanceGuid,
                              action             : Audit.AuditAction.Deprovision,
                              principal          : principal.name,
                              async              : acceptsIncomplete,
                              failed             : failed
                      ])
        }
    }

    private DeprovisionRequest createDeprovisionRequest(String serviceInstanceGuid, boolean acceptsIncomplete) {
        return new DeprovisionRequest(serviceInstanceGuid: serviceInstanceGuid,
                                      serviceInstance: controllerHelper.getAndCheckServiceInstance(serviceInstanceGuid),
                                      acceptsIncomplete: acceptsIncomplete)
    }

    @ApiOperation(value = "Get the last operation status", response = LastOperationResponseDto.class,
            notes = "Returns the last operation status for the given service instance")
    @RequestMapping(value = "/v2/service_instances/{serviceInstanceGuid}/last_operation", method = RequestMethod.GET)
    LastOperationResponseDto lastOperation(
            @PathVariable("serviceInstanceGuid") String serviceInstanceGuid,
            @RequestParam(value = "operation", required = false) String operationId) {
        return lastOperationStatusService.pollJobStatus(serviceInstanceGuid)
    }

    @ApiOperation(value = "Fetch service instance", response = ServiceInstanceResponseDto.class)
    @RequestMapping(value = "/v2/service_instances/{serviceInstanceGuid}", method = RequestMethod.GET)
    ServiceInstanceResponseDto getServiceInstance(@PathVariable("serviceInstanceGuid") String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (serviceInstance == null || serviceInstance.isDeleted()) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        } else if (!serviceInstance.plan.service.instancesRetrievable) {
            LOGGER.warn("Service Instance Fetch requested for Service Instance {} not supporting fetching",
                        serviceInstanceGuid)
            ErrorCode.SERVICE_INSTANCE_NOT_RETRIEVABLE.throwNew()
        } else if (!serviceInstance.isCompleted()) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }

        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (!(serviceProvider instanceof FetchServiceInstanceProvider)) {
            return new ServiceInstanceDtoConverter().convert(serviceInstance)
        } else {
            FetchServiceInstanceProvider provider = serviceProvider as FetchServiceInstanceProvider
            return provider.fetchServiceInstance(serviceInstance)
        }
    }

}