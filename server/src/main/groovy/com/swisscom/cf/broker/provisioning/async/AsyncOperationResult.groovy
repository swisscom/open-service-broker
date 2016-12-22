package com.swisscom.cf.broker.provisioning.async

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.provisioning.state.ServiceState
import groovy.transform.CompileStatic

@CompileStatic
class AsyncOperationResult {
    LastOperation.Status status
    String description
    String internalStatus
    Collection<ServiceDetail> details = []

    static AsyncOperationResult of(ServiceState serviceState,Collection<ServiceDetail> details = []){
        return new AsyncOperationResult(status: serviceState.lastOperationStatus,
                                        internalStatus: serviceState.serviceInternalState, details:details)
    }
}
