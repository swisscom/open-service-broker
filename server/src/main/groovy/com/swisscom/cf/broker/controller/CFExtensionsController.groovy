package com.swisscom.cf.broker.controller

import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.google.common.base.Optional
import com.swisscom.cf.broker.error.ErrorCode
import com.swisscom.cloud.servicebroker.model.endpoint.Endpoint
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointService
import com.swisscom.cloud.servicebroker.model.usage.ServiceUsage
import com.swisscom.cf.broker.cfextensions.serviceusage.ServiceUsageLookup
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CompileStatic
@Api(value = "cf-extensions", description = "Additional operations for service instances")
@RestController
class CFExtensionsController extends BaseController {
    public static final String PARAM_END_DATE = 'end_date'

    @Autowired
    protected ServiceUsageLookup serviceUsageLookup

    @Autowired
    protected EndpointService endpointLookup

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @ApiOperation(value = "Get service instance usage", response = ServiceUsage.class)
    @RequestMapping(value = ['/v2/cf-ext/{service_instance}/usage', //deprecated, prefer the path below
                            '/custom/service_instances/{service_instance}/usage'],
            method = RequestMethod.GET)
    def usage(
            @PathVariable('service_instance') String serviceInstanceId,
            @RequestParam(value = 'end_date', required = false) String enddate) {
        return serviceUsageLookup.usage(findServiceInstance(serviceInstanceId), parseEnddate(enddate))
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
    @RequestMapping(value = ['/v2/cf-ext/{service_instance}/endpoint',//deprecated, prefer the path below
                            '/custom/service_instances/{service_instance}/endpoint'],
                    method = RequestMethod.GET)
    def endpoint(@PathVariable('service_instance') String serviceInstanceId) {
        endpointLookup.lookup(findServiceInstance(serviceInstanceId))
    }

    private ServiceInstance findServiceInstance(String serviceInstanceGuid) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceGuid)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()
        }
        return serviceInstance
    }
}