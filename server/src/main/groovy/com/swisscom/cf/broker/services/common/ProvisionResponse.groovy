package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class ProvisionResponse {
    Collection<ServiceDetail> details
    boolean isAsync
    String dashboardURL
}
