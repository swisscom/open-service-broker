package com.swisscom.cf.broker.controller

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.servicedefinition.ServiceDefinitionProcessor
import com.swisscom.cf.broker.servicedefinition.dto.ServiceDto
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@CompileStatic
@Log4j
class ServiceDefinitionController extends BaseController {
    @VisibleForTesting
    protected ServiceDefinitionProcessor serviceDefinitionProcessor


    @ApiOperation(value = "Add/Update service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition', //deprecated, prefer the path below
            '/custom/admin/service-definition/{service_id}'],
            method = [RequestMethod.POST,RequestMethod.PUT])
    void createOrUpdate(@RequestBody String text) {
        serviceDefinitionProcessor.createOrUpdateServiceDefinition(text)
    }

    @ApiOperation(value = "Get service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition/{service_id}', //deprecated, prefer the path below
                             '/custom/admin/service-definition/{service_id}'],
            method = RequestMethod.GET)
    ServiceDto get(@PathVariable('service_id') String serviceId) {
        return serviceDefinitionProcessor.getServiceDefinition(serviceId)
    }


    @ApiOperation(value = "Delete service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition/{service_id}', //deprecated, prefer the path below
            '/custom/admin/service-definition/{service_id}'],
            method = RequestMethod.DELETE)
    void delete(@PathVariable('id') String serviceId) {
        serviceDefinitionProcessor.deleteServiceDefinition(serviceId)
    }
}
