package com.swisscom.cloud.servicebroker.client

import com.swisscom.cloud.sb.model.endpoint.Endpoint
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClientExtended extends IServiceBrokerClient {
    ResponseEntity<Endpoint> getEndpoint(String serviceInstanceId)
    ResponseEntity<ServiceUsage> getUsage(String serviceInstanceId)
    ResponseEntity<Void> createOrUpdateServiceDefinition(String definition)
}
