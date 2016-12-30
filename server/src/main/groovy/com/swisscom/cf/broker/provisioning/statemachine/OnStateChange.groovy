package com.swisscom.cf.broker.provisioning.statemachine

import groovy.transform.CompileStatic


@CompileStatic
interface OnStateChange<T extends StateMachineContext> {
    ActionResult triggerAction(T context)
}
