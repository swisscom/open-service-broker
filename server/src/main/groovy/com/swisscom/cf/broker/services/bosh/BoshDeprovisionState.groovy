package com.swisscom.cf.broker.services.bosh

import com.google.common.base.Optional
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.state.ServiceState
import groovy.transform.CompileStatic

@CompileStatic
enum BoshDeprovisionState implements ServiceState {
    BOSH_INITIAL(LastOperation.Status.IN_PROGRESS),
    BOSH_DEPLOYMENT_DELETION_REQUESTED(LastOperation.Status.IN_PROGRESS),
    BOSH_TASK_SUCCESSFULLY_FINISHED(LastOperation.Status.IN_PROGRESS),
    BOSH_CLOUD_CONFIG_UPDATED(LastOperation.Status.IN_PROGRESS),
    CLOUD_PROVIDER_SERVER_GROUP_DELETED(LastOperation.Status.IN_PROGRESS)

    final LastOperation.Status status

    BoshDeprovisionState(final LastOperation.Status status) {
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

    static Optional<BoshDeprovisionState> of(String text) {
        def result = BoshDeprovisionState.values().find { it.name() == text }
        if (!result) {
            return Optional.absent()
        }
        return Optional.of(result)
    }
}
