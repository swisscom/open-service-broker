package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class StateChangeActionResult {
    Collection<ServiceDetail> details
    boolean go2NextState
}
