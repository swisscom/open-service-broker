package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class StateChangeActionResult {
    String message
    Collection<ServiceDetail> details
    boolean go2NextState
}
