package com.swisscom.cf.broker.services.mongodb.enterprise.v2

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.services.bosh.BoshDeprovisionState
import com.swisscom.cf.broker.services.common.ServiceState

enum MongoDbEnterpriseDeprovisionStateV2 implements ServiceState {
    INITIAL(LastOperation.Status.IN_PROGRESS),
    AUTOMATION_UPDATE_REQUESTED(LastOperation.Status.IN_PROGRESS),
    AUTOMATION_UPDATED(LastOperation.Status.IN_PROGRESS),
    HOSTS_DELETED(LastOperation.Status.IN_PROGRESS),
    NODE_NAMES_GONE_FROM_DNS(LastOperation.Status.IN_PROGRESS),
    DEPROVISION_SUCCESS(LastOperation.Status.SUCCESS)

    public static final Map<String, ServiceState> map = new TreeMap<String, ServiceState>()

    static {
        for (ServiceState serviceState : values() + BoshDeprovisionState.values()) {
            if (map.containsKey(serviceState.getServiceState())) {
                throw new RuntimeException("Enum:${serviceState.getServiceState()} already exists in:${MongoDbEnterpriseDeprovisionStateV2.class.getSimpleName()}!")
            } else {
                map.put(serviceState.getServiceState(), serviceState);
            }
        }
    }

    private final LastOperation.Status status

    MongoDbEnterpriseDeprovisionStateV2(LastOperation.Status lastOperationStatus) {
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