package com.swisscom.cf.broker.services.bosh

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.statemachine.ServiceState
import groovy.transform.CompileStatic

@CompileStatic
enum BoshProvisionState implements ServiceState {
    BOSH_INITIAL(LastOperation.Status.IN_PROGRESS),
    CLOUD_PROVIDER_SERVER_GROUP_CREATED(LastOperation.Status.IN_PROGRESS),
    BOSH_CLOUD_CONFIG_UPDATED(LastOperation.Status.IN_PROGRESS),
    BOSH_DEPLOYMENT_TRIGGERED(LastOperation.Status.IN_PROGRESS),
    BOSH_TASK_SUCCESSFULLY_FINISHED(LastOperation.Status.IN_PROGRESS)

    final LastOperation.Status status

    BoshProvisionState(final LastOperation.Status status) {
        this.status = status
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }

    static Optional<BoshProvisionState> of(String text) {
        def result = BoshProvisionState.values().find { it.name() == text }
        if (!result) {
            return Optional.absent()
        }
        return Optional.of(result)
    }
}
