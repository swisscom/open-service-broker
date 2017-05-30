package com.swisscom.cloud.sb.broker.provisioning.statemachine

import com.swisscom.cloud.sb.broker.model.LastOperation

trait ServiceState {
    abstract LastOperation.Status getLastOperationStatus()
    abstract String getServiceInternalState()
}
