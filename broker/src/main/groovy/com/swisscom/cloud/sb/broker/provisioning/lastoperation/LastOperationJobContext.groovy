package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class LastOperationJobContext {

    @Autowired
    private LastOperationRepository lastOperationRepository

    LastOperation lastOperation
    ServiceInstance serviceInstance
    Plan plan
    ProvisionRequest provisionRequest
    DeprovisionRequest deprovisionRequest
    UpdateRequest updateRequest

    void notifySuccess(String message = null) {
        notifyResult(true, message)
    }

    void notifyFailure(String message = null) {
        notifyResult(false, message)
    }

    void notifyResult(boolean success, String message = null) {
        updateJob(success ? LastOperation.Status.SUCCESS : LastOperation.Status.FAILED, message)
    }

    void notifyProgress(String message = null, String internalState = null) {
        updateJob(LastOperation.Status.IN_PROGRESS, message, internalState)
    }

    private void updateJob(LastOperation.Status status, String message = null, String internalState = null) {
        lastOperation = lastOperationRepository.merge(lastOperation)
        lastOperation.status = status
        lastOperation.description = message
        lastOperation.internalState = internalState
        lastOperationRepository.saveAndFlush(lastOperation)
    }
}

