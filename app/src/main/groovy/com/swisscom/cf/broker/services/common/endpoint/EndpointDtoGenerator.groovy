package com.swisscom.cf.broker.services.common.endpoint

import com.swisscom.cf.broker.filterextensions.endpoint.EndpointDto
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.util.ServiceDetailType
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class EndpointDtoGenerator {
    public static final Collection<String> DEFAULT_PROTOCOLS = Collections.unmodifiableCollection(["tcp"])

    Collection<EndpointDto> findEndpoints(ServiceInstance serviceInstance, EndpointConfig config) {
        def result = new LinkedList<EndpointDto>()
        def portsForServiceInstance = ServiceDetailsHelper.from(serviceInstance).findAllWithServiceDetailType(ServiceDetailType.PORT)
        parseProtocols(config).each { String protocol ->
            portsForServiceInstance.each {
                String port ->
                    result.add(new EndpointDto(protocol: protocol, ports: port, destination: config.ipRange))
            }
        }

        return result
    }

    private Collection<String> parseProtocols(EndpointConfig config) {
        def result = config.protocols?.split(",")
        if (result == null || result.size() == 0) {
            return DEFAULT_PROTOCOLS
        }
        return Arrays.asList(result)
    }
}
