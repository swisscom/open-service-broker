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

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cloud.sb.broker.metrics.BindingMetricsService
import com.swisscom.cloud.sb.broker.metrics.LifecycleTimeMetricsService
import com.swisscom.cloud.sb.broker.metrics.ServiceInstanceMetricsService
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.servicedefinition.ServiceDefinitionProcessor
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 * Any modification of the service definition during runtime is considered dangerous, as the  ServiceDefinitionInitializer
 * overwrites service definitions on startup.
 */

@Api(value = "Service definition", description = "Endpoint for service definition")
@RestController
@CompileStatic
@Slf4j
@Deprecated
class ServiceDefinitionController extends BaseController {
    @VisibleForTesting
    @Autowired
    private ServiceDefinitionProcessor serviceDefinitionProcessor
    @Autowired
    CFServiceRepository cfServiceRepository
    @Autowired
    ServiceBindingRepository serviceBindingRepository
    @Autowired
    BindingMetricsService bindingMetricsService
    @Autowired
    LifecycleTimeMetricsService lifecycleTimeMetrics
    @Autowired
    ServiceInstanceMetricsService serviceInstanceMetricsService

    @ApiOperation(value = "Add/Update service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition', //deprecated, prefer the path below
            '/custom/admin/service-definition'],
            method = [RequestMethod.POST,RequestMethod.PUT],
            headers = "content-type=application/json")
    void createOrUpdate(@RequestBody String text) {
        serviceDefinitionProcessor.createOrUpdateServiceDefinition(text)
        registerNewServiceWithMetricsService()
    }

    void registerNewServiceWithMetricsService(){
        bindingMetricsService.bindAll()
        lifecycleTimeMetrics.bindAll()
        serviceInstanceMetricsService.bindAll()
    }

    @ApiOperation(value = "Get service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition/{serviceInstanceGuid}', //deprecated, prefer the path below
                             '/custom/admin/service-definition/{serviceInstanceGuid}'],
            method = RequestMethod.GET)
    ServiceDto get(@PathVariable('serviceInstanceGuid') String serviceId) {
        return serviceDefinitionProcessor.getServiceDefinition(serviceId)
    }


    @ApiOperation(value = "Delete service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition/{serviceInstanceGuid}', //deprecated, prefer the path below
            '/custom/admin/service-definition/{serviceInstanceGuid}'],
            method = RequestMethod.DELETE)
    void delete(@PathVariable('serviceInstanceGuid') String serviceId) {
        serviceDefinitionProcessor.deleteServiceDefinition(serviceId)
    }
}
