package com.swisscom.cf.broker.provisioning.statemachine.action

import com.swisscom.cf.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.StateMachineContext
import groovy.transform.CompileStatic


@CompileStatic
class NoOp<T extends StateMachineContext> implements OnStateChange<T> {
    @Override
    StateChangeActionResult triggerAction(T context) {
        return new StateChangeActionResult(go2NextState: true)
    }
}
