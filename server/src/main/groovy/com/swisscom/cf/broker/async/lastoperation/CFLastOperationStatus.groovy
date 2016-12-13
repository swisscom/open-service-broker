package com.swisscom.cf.broker.async.lastoperation

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonValue
import groovy.transform.CompileStatic

@CompileStatic
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum CFLastOperationStatus {
    IN_PROGRESS('in progress'),
    SUCCEEDED('succeeded'),
    FAILED('failed')

    final String status

    CFLastOperationStatus(String status) { this.status = status }

    static CFLastOperationStatus of(String status) {
        return CFLastOperationStatus.values().find { it.status == status }
    }

    @Override
    @JsonValue
    public String toString() {
        return status;
    }
}