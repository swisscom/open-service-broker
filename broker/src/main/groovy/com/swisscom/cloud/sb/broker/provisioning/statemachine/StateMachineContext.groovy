package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import groovy.transform.CompileStatic

@CompileStatic
abstract class StateMachineContext {
    LastOperationJobContext lastOperationJobContext
}
