/*
 * Copyright (c) 2019 Swisscom (Switzerland) Ltd.
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

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ServiceInstanceCleanup
import com.swisscom.cloud.sb.broker.services.LastOperationService
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Api(value = "Admin", description = "Endpoint for admin operations")
@RestController
@CompileStatic
class AdminController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class)

    private final LastOperationService lastOperationService
    private final ServiceInstanceCleanup serviceInstanceCleanup

    @Autowired
    AdminController(LastOperationService lastOperationService,
                    ServiceInstanceCleanup serviceInstanceCleanup) {
        this.lastOperationService = lastOperationService
        this.serviceInstanceCleanup = serviceInstanceCleanup
    }

    @ApiOperation(value = "Terminate Last Operation")
    @RequestMapping(value = 'admin/service_instances/{serviceInstanceGuid}/last_operation/terminate',
            method = RequestMethod.POST)
    void terminateLastOperation(@PathVariable('serviceInstanceGuid') String serviceInstanceGuid) {
        LOGGER.info("Request to terminate last operation for service instance '{}'", serviceInstanceGuid)
        lastOperationService.terminateLastOperation(serviceInstanceGuid)
    }

    @ApiOperation(value = "Purge Service Instance")
    @RequestMapping(value = 'admin/service_instances/{serviceInstanceGuid}/purge', method = RequestMethod.DELETE)
    ServiceInstance purgeServiceInstance(@PathVariable('serviceInstanceGuid') String serviceInstanceGuid) {
        LOGGER.info("Request to purge service instance '{}'", serviceInstanceGuid)
        return serviceInstanceCleanup.markServiceInstanceForPurge(serviceInstanceGuid)
    }
}
