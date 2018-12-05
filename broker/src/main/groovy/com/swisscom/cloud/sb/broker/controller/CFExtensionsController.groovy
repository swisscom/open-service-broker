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

import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointService
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageLookup
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.yaml.snakeyaml.Yaml

@CompileStatic
@Api(value = "cf-extensions", description = "Additional operations for service instances")
@RestController
class CFExtensionsController extends BaseController {
    @Autowired
    protected ServiceUsageLookup serviceUsageLookup

    @Autowired
    protected EndpointService endpointLookup

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceProviderLookup serviceProviderLookup

    @ApiOperation(value = "Get service instance usage", response = ServiceUsage.class)
    @RequestMapping(value = ['/v2/cf-ext/{serviceInstanceGuid}/usage', //deprecated, prefer the path below
                            '/custom/service_instances/{service_instance}/usage'],
            method = RequestMethod.GET)
    def usage(
            @PathVariable('serviceInstanceGuid') String serviceInstanceGuid,
            @RequestParam(value = 'end_date', required = false) String enddate) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew("ID = ${serviceInstanceGuid}")
        }
        return serviceUsageLookup.usage(serviceInstance, parseEnddate(enddate))
    }

    private Optional<Date> parseEnddate(String enddate) {
        if (enddate) {
            return Optional.of(new ISO8601DateFormat().parse(enddate))
        } else {
            return Optional.absent()
        }
    }

    @ApiOperation(value = "Get endpoint information about a service", response = Endpoint.class,
            notes = "provides information to create security groups for a given service instance", responseContainer = "List")
    @RequestMapping(value = ['/v2/cf-ext/{serviceInstanceGuid}/endpoint',//deprecated, prefer the path below
                            '/custom/service_instances/{serviceInstanceGuid}/endpoint'],
                    method = RequestMethod.GET)
    def endpoint(@PathVariable('serviceInstanceGuid') String serviceInstanceGuid) {
        endpointLookup.lookup(getAndCheckServiceInstance(serviceInstanceGuid))
    }

    @ApiOperation(value = "Get extension information", response = Yaml.class,
            notes = "provides openapi 3.0 yaml")
    @RequestMapping(value = ['/custom/service_instances/{serviceInstanceGuid}/api-docs'],
            method = RequestMethod.GET)
    def getApi(@PathVariable('serviceInstanceGuid') String serviceInstanceGuid) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (serviceInstance) {
            ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
            if (!(serviceProvider instanceof ExtensionProvider)) {
                throw new RuntimeException("Service provider: ${serviceProvider.class.name} does not provide extension information")
            }
            ExtensionProvider exProvider = serviceProvider as ExtensionProvider

            return exProvider.getApi()
        } else {
            ExtensionProvider serviceProvider = serviceProviderLookup.findServiceProvider(new Plan(serviceProviderClass: "dummyExtensionsServiceProvider")) as ExtensionProvider
            return serviceProvider.getApi()
        }
    }

}