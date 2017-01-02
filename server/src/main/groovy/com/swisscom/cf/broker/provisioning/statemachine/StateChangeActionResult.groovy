package com.swisscom.cf.broker.provisioning.statemachine

import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class StateChangeActionResult {
    Collection<ServiceDetail> details
    boolean go2NextState
}
