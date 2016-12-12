package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.services.common.endpoint.EndpointConfig
import groovy.transform.CompileStatic

@CompileStatic
trait BoshBasedServiceConfig implements EndpointConfig, BoshConfig {
    int retryIntervalInSeconds
    int maxRetryDurationInMinutes
    String portRange
    String openstackkUrl
    String openstackUsername
    String openstackPassword
    String openstackTenantName
    String boshManifestFolder
}
