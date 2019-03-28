package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext

interface MinimalStateMachineContext {
    LastOperationJobContext lastOperationJobContext
}
