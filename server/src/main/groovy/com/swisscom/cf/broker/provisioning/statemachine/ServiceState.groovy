package com.swisscom.cf.broker.provisioning.statemachine

import com.swisscom.cf.broker.model.LastOperation

trait ServiceState {
    abstract LastOperation.Status getLastOperationStatus()
    abstract String getServiceInternalState()
}
