package com.swisscom.cf.broker.backup

import groovy.transform.CompileStatic

@CompileStatic
enum RestoreStatus implements Serializable {

    IN_PROGRESS('IN_PROGRESS'),
    SUCCEEDED('SUCCEEDED'),
    FAILED('FAILED')

    final String status

    RestoreStatus(String status) { this.status = status }

    static RestoreStatus of(String status) {
        return RestoreStatus.values().find { it.status == status }
    }

    @Override
    public String toString() {
        return status;
    }
}