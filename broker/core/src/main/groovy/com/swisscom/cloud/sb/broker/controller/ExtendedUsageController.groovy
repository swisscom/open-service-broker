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

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.usage.ExtendedServiceUsageLookup
import com.swisscom.cloud.sb.model.usage.extended.ServiceUsageItem
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Api(value = "Service provisioning", description = "Endpoint for provisioning/deprovisoning")
@RestController
@CompileStatic
@Slf4j
class ExtendedUsageController extends BaseController {

    private final ServiceInstanceRepository serviceInstanceRepository
    private final ExtendedServiceUsageLookup extendedServiceUsageLookup

    ExtendedUsageController(ServiceInstanceRepository serviceInstanceRepository, ExtendedServiceUsageLookup extendedServiceUsageLookup) {
        this.extendedServiceUsageLookup = extendedServiceUsageLookup
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    @ApiOperation(value = "Gets list of usage information for a service instance",
            response = ServiceUsageItem.class,
            responseContainer = "List")
    @RequestMapping(value = '/v2/service_instances/{serviceInstanceGuid}/usage', method = RequestMethod.GET)
    ResponseEntity<Set<ServiceUsageItem>> provision(@PathVariable("serviceInstanceGuid") String serviceInstanceGuid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }

        new ResponseEntity<Set<ServiceUsageItem>>(extendedServiceUsageLookup.getUsage(serviceInstance), HttpStatus.OK)
    }

}
