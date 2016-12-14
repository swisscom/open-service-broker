package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.services.bosh.BoshProvisionState
import com.swisscom.cf.broker.services.common.ServiceState

enum MongoDbEnterpriseProvisionState implements ServiceState {
    INITIAL(LastOperation.Status.IN_PROGRESS),
    OPS_MANAGER_GROUP_CREATED(LastOperation.Status.IN_PROGRESS),
    AGENTS_READY(LastOperation.Status.IN_PROGRESS),
    AUTOMATION_UPDATE_REQUESTED(LastOperation.Status.IN_PROGRESS),
    PROVISION_SUCCESS(LastOperation.Status.SUCCESS)

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshProvisionState.values()) {
            if (map.containsKey(serviceState.getServiceState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceState()} already exists in:${MongoDbEnterpriseProvisionState.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceState(), serviceState);
            }
        }
    }

    private final LastOperation.Status status

    MongoDbEnterpriseProvisionState(LastOperation.Status lastOperationStatus) {
        this.status = lastOperationStatus
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceState() {
        return name()
    }

    public static ServiceState of(String state) {
        return map.get(state);
    }
}