package com.swisscom.cloud.sb.broker.controller

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cloud.sb.broker.servicedefinition.ServiceDefinitionProcessor
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@Api(value = "Service definition", description = "Endpoint for service definition")
@RestController
@CompileStatic
@Slf4j
class ServiceDefinitionController extends BaseController {
    @VisibleForTesting
    @Autowired
    private ServiceDefinitionProcessor serviceDefinitionProcessor

    @ApiOperation(value = "Add/Update service definition", response = ServiceDto)
    @RequestMapping(value = ['/service-definition', //deprecated, prefer the path below
            '/custom/admin/service-definition'],
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
    void delete(@PathVariable('service_id') String serviceId) {
        serviceDefinitionProcessor.deleteServiceDefinition(serviceId)
    }
}
