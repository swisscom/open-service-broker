package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.cfapi.dto.ProvisioningDto
import com.swisscom.cloud.sb.broker.cfapi.dto.UpdateDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.updating.UpdateResponseDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@Api(value = "Service instance updating", description = "Endpoint for updating parameters and plan on a service instance.")
@RestController
class UpdatingController extends BaseController {
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @ApiOperation(value = "Updates an existing service instance.", response = UpdateResponseDto.class)
    @RequestMapping(value = '/v2/service_instances/{instanceId}', method = RequestMethod.PATCH)
    ResponseEntity<UpdateResponseDto> update(@PathVariable("instanceId") String serviceInstanceGuid,
                                             @RequestParam(value = 'accepts_incomplete', required = false) boolean acceptsIncomplete,
                                             @Valid @RequestBody UpdateDto updateDto) {

        log.info("Provision request for ServiceInstanceGuid:${serviceInstanceGuid}, ServiceId: ${updateDto?.service_id}, Params: ${updateDto.parameters}")
        ServiceInstance serviceInstance = getServiceInstanceOrFail(serviceInstanceGuid)


    }

    private ServiceInstance getServiceInstanceOrFail(String serviceInstanceGuid) {
        ServiceInstance instance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!instance) {
            log.debug "Service instance with id ${instance.guid} does not exist - returning 410 GONE"
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew();
        }
        return instance
    }
}
