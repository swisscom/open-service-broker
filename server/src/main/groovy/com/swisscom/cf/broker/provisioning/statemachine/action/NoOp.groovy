package com.swisscom.cf.broker.provisioning.statemachine.action

import com.swisscom.cf.broker.provisioning.statemachine.ActionResult
import com.swisscom.cf.broker.provisioning.statemachine.OnStateChange
import com.swisscom.cf.broker.provisioning.statemachine.StateMachineContext
import groovy.transform.CompileStatic


@CompileStatic
class NoOp implements OnStateChange {
    @Override
    ActionResult triggerAction(StateMachineContext context) {
        return new ActionResult(success: true)
    }
}
