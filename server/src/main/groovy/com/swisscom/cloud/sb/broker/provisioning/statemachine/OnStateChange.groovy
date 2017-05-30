package com.swisscom.cloud.sb.broker.provisioning.statemachine

import groovy.transform.CompileStatic


@CompileStatic
interface OnStateChange<T extends StateMachineContext> {
    StateChangeActionResult triggerAction(T context)
}
