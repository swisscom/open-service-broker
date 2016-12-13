package com.swisscom.cf.servicebroker.client

import com.swisscom.cf.servicebroker.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClientExtended extends IServiceBrokerClient {
    ResponseEntity<Endpoint> getEndpoint(String serviceInstanceId)
}
