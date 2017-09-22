package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class EndpointLookup {
    public static final Collection<String> DEFAULT_PROTOCOLS = Collections.unmodifiableCollection(["tcp"])

    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance, EndpointConfig config) {
        def result = new LinkedList<Endpoint>()
        def portsForServiceInstance = ServiceDetailsHelper.from(serviceInstance).findAllWithServiceDetailType(ServiceDetailType.PORT)
        if (config.protocols.size() == 0){
            config.protocols.addAll(DEFAULT_PROTOCOLS)
        }

        config.protocols.each { String protocol ->
            config.ipRanges.each { String ipRange ->
                portsForServiceInstance.each { String port ->
                        result.add(new Endpoint(protocol: protocol, ports: port, destination: ipRange))
                }
            }
        }
        return result
    }
}
