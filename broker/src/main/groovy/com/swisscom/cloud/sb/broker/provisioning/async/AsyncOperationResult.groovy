package com.swisscom.cloud.sb.broker.provisioning.async

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.provisioning.statemachine.ServiceState
import groovy.transform.CompileStatic

@CompileStatic
class AsyncOperationResult {
    LastOperation.Status status
    String description
    String internalStatus
    Collection<ServiceDetail> details = []

    static AsyncOperationResult of(ServiceState serviceState, Collection<ServiceDetail> details = []) {
        return new AsyncOperationResult(
                status: serviceState.lastOperationStatus,
                internalStatus: serviceState.serviceInternalState,
                details: details)
    }

    static AsyncOperationResult of(ServiceState serviceState, Collection<ServiceDetail> details = [], String description) {
        return new AsyncOperationResult(
                status: serviceState.lastOperationStatus,
                internalStatus: serviceState.serviceInternalState,
                details: details,
                description: description)
    }
}
