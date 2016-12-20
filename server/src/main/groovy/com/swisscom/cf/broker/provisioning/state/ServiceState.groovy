package com.swisscom.cf.broker.provisioning.state

import com.swisscom.cf.broker.model.LastOperation

interface ServiceState {
    LastOperation.Status getLastOperationStatus()
    String getServiceState()


}
