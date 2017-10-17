package com.swisscom.cloud.sb.broker.services

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import groovy.transform.CompileStatic

@CompileStatic
//TODO change this to inherit from Config and *NOT* EndpointConfig
trait AsyncServiceConfig implements EndpointConfig {
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
}
