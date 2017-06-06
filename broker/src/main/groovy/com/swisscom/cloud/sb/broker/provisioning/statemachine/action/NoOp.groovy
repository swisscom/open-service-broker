package com.swisscom.cloud.sb.broker.provisioning.statemachine.action

import com.swisscom.cloud.sb.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateChangeActionResult
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineContext
import groovy.transform.CompileStatic


@CompileStatic
class NoOp<T extends StateMachineContext> implements OnStateChange<T> {
    @Override
    StateChangeActionResult triggerAction(T context) {
        return new StateChangeActionResult(go2NextState: false)
    }
}
