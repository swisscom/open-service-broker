package com.swisscom.cf.broker.provisioning.statemachine

import groovy.transform.CompileStatic


@CompileStatic
interface OnStateChange {
    ActionResult triggerAction(StateMachineContext context)
}
