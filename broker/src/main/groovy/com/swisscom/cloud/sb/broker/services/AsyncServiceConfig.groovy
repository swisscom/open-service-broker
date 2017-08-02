package com.swisscom.cloud.sb.broker.services

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import groovy.transform.CompileStatic

@CompileStatic
trait AsyncServiceConfig implements EndpointConfig {
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
}
