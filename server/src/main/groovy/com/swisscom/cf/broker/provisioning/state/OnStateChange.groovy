package com.swisscom.cf.broker.provisioning.state

import groovy.transform.CompileStatic


@CompileStatic
interface OnStateChange {
    ActionResult triggerAction(StateContext context)
}
