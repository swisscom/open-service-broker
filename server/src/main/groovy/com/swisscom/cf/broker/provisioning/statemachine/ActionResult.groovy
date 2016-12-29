package com.swisscom.cf.broker.provisioning.statemachine

import com.swisscom.cf.broker.model.ServiceDetail
import groovy.transform.CompileStatic

@CompileStatic
class ActionResult {
    Collection<ServiceDetail> details
    boolean success
}
