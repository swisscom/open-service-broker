package com.swisscom.cf.broker.services.common

import com.swisscom.cf.broker.model.LastOperation

interface ServiceState {
    LastOperation.Status getLastOperationStatus()

    String getServiceState()
}
