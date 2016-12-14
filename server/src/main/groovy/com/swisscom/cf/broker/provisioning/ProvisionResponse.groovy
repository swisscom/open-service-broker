package com.swisscom.cf.broker.provisioning

import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class ProvisionResponse {
    Collection<ServiceDetail> details
    boolean isAsync
    String dashboardURL
}
