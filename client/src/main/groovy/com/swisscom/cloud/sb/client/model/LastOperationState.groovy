package com.swisscom.cloud.sb.client.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/*
* This class would not be needed if the model object with the link below could be deserialized correctly
 * https://github.com/spring-cloud/spring-cloud-cloudfoundry-service-broker/blob/master/src/main/java/org/springframework/cloud/servicebroker/model/OperationState.java
* */

enum LastOperationState {

    IN_PROGRESS("in progress"),
    SUCCEEDED("succeeded"),
    FAILED("failed")

    private final String state

    LastOperationState(String state) {
        this.state = state
    }

    @JsonCreator
    public static LastOperationState fromString(String key) {
        return key ? LastOperationState.values().find { it.value == key } : null
    }

    @JsonValue
    public String getValue() {
        return state
    }

}

