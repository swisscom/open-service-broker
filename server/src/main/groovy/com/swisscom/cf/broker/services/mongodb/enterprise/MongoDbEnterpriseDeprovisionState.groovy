package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.services.bosh.BoshDeprovisionState
import com.swisscom.cf.broker.provisioning.statemachine.ServiceState

enum MongoDbEnterpriseDeprovisionState implements ServiceState {
    INITIAL(LastOperation.Status.IN_PROGRESS),
    AUTOMATION_UPDATE_REQUESTED(LastOperation.Status.IN_PROGRESS),
    AUTOMATION_UPDATED(LastOperation.Status.IN_PROGRESS),
    HOSTS_DELETED(LastOperation.Status.IN_PROGRESS),
    CLEAN_UP_GROUP(LastOperation.Status.IN_PROGRESS),
    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS)

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshDeprovisionState.values()) {
            if (map.containsKey(serviceState.getServiceInternalState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceInternalState()} already exists in:${MongoDbEnterpriseDeprovisionState.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceInternalState(), serviceState);
            }
        }
    }

    private final LastOperation.Status status

    MongoDbEnterpriseDeprovisionState(LastOperation.Status lastOperationStatus) {
        this.status = lastOperationStatus
    }

    @Override
    LastOperation.Status getLastOperationStatus() {
        return status
    }

    @Override
    String getServiceInternalState() {
        return name()
    }

    public static ServiceState of(String state) {
        return map.get(state);
    }
}