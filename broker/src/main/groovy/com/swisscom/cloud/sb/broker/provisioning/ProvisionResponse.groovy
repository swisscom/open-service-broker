package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class ProvisionResponse {
    Collection<ServiceDetail> details
    boolean isAsync
    String dashboardURL
    String operation
}
