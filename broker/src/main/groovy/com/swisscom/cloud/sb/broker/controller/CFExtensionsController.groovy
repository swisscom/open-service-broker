package com.swisscom.cloud.sb.broker.controller

import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointService
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageLookup
import com.swisscom.cloud.sb.broker.cfextensions.serviceusage.ServiceUsageProvider
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
    public static final String PARAM_END_DATE = 'end_date'

    @Autowired
    protected ServiceUsageLookup serviceUsageLookup

    @Autowired
    protected EndpointService endpointLookup

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceProviderLookup serviceProviderLookup

    private ExtensionProvider extensionProvider

    @ApiOperation(value = "Get service instance usage", response = ServiceUsage.class)
    @RequestMapping(value = ['/v2/cf-ext/{service_instance}/usage', //deprecated, prefer the path below
                            '/custom/service_instances/{service_instance}/usage'],
            method = RequestMethod.GET)
    def usage(
            @PathVariable('service_instance') String serviceInstanceId,
            @RequestParam(value = 'end_date', required = false) String enddate) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceId)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew("ID = ${serviceInstanceId}")
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
    @RequestMapping(value = ['/v2/cf-ext/{service_instance}/endpoint',//deprecated, prefer the path below
                            '/custom/service_instances/{service_instance}/endpoint'],
                    method = RequestMethod.GET)
    def endpoint(@PathVariable('service_instance') String serviceInstanceId) {
        endpointLookup.lookup(getAndCheckServiceInstance(serviceInstanceId))
    }

    @ApiOperation(value = "Get extension information", response = Yaml.class,
            notes = "provides openapi 3.0 yaml")
    @RequestMapping(value = ['/custom/service_instances/{service_instance}/api-docs'],
            method = RequestMethod.GET)
    def getApi(@PathVariable('service_instance') String serviceInstanceId) {
//        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceId)
//        if (serviceInstance) {
//            ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
//            if (!(serviceProvider instanceof ExtensionProvider)) {
//                throw new RuntimeException("Service provider: ${serviceProvider.class.name} does not provide extension information")
//            }
//            ExtensionProvider exProvider = serviceProvider as ExtensionProvider
//
//            return exProvider.getApi()
//        } else {
//            return extensionProvider.getApi()
//        }

        ExtensionProvider serviceProvider = serviceProviderLookup.findServiceProvider(new Plan(serviceProviderClass: "dummyExtensionsServiceProvider")) as ExtensionProvider
        return serviceProvider.getApi()
    }

}