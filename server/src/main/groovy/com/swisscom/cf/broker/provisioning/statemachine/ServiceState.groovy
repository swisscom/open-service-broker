package com.swisscom.cf.broker.provisioning.statemachine

import com.swisscom.cf.broker.model.LastOperation

interface ServiceState {
    LastOperation.Status getLastOperationStatus()
    String getServiceInternalState()
}
