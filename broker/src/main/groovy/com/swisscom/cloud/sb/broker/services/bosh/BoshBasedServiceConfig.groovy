package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.cfextensions.endpoint.EndpointConfig
import groovy.transform.CompileStatic

@CompileStatic
trait BoshBasedServiceConfig implements EndpointConfig, BoshConfig {
    String portRange
    String openstackkUrl
    String openstackUsername
    String openstackPassword
    String openstackTenantName
    String boshManifestFolder
    boolean shuffleAzs
}
