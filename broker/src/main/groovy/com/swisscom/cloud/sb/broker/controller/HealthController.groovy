package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.services.health.ServiceHealthProviderLookup
import com.swisscom.cloud.sb.model.health.ServiceHealth
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Api(value = "Service instance Health", description = "Endpoint for getting ServiceHealth for ServiceInstance")
@RestController
@CompileStatic
@Slf4j
class HealthController extends BaseController {

    private final ServiceHealthProviderLookup serviceHealthProviderLookup

    @Autowired
    HealthController(ServiceHealthProviderLookup serviceHealthProviderLookup) {
        this.serviceHealthProviderLookup = serviceHealthProviderLookup
    }

    @ApiOperation(value = "Get ServiceHealth for a service instance ", response = ServiceHealth.class)
    @RequestMapping(value = "/v2/service_instances/{instanceId}/health", method = RequestMethod.GET)
    ServiceHealth health(@PathVariable("instanceId") String serviceInstanceGuid) {
        return serviceHealthProviderLookup.getHealthForServiceInstance(serviceInstanceGuid)
    }
}
