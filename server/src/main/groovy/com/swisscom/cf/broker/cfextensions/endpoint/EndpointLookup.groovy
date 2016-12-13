package com.swisscom.cf.broker.cfextensions.endpoint

import com.swisscom.cf.broker.cfextensions.endpoint.Endpoint
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointConfig
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.util.ServiceDetailType
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class EndpointLookup {
    public static final Collection<String> DEFAULT_PROTOCOLS = Collections.unmodifiableCollection(["tcp"])

    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance, EndpointConfig config) {
        def result = new LinkedList<Endpoint>()
        def portsForServiceInstance = ServiceDetailsHelper.from(serviceInstance).findAllWithServiceDetailType(ServiceDetailType.PORT)
        parseProtocols(config).each { String protocol ->
            portsForServiceInstance.each {
                String port ->
                    result.add(new Endpoint(protocol: protocol, ports: port, destination: config.ipRange))
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
