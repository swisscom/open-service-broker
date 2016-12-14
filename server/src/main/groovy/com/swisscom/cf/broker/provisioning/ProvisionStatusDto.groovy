package com.swisscom.cf.broker.provisioning

import groovy.transform.CompileStatic

@CompileStatic
class ProvisionStatusDto implements Serializable {
    OperationState state
    String description


    public enum OperationState {
        IN_PROGRESS, SUCCEEDED, FAILED
    }
}
