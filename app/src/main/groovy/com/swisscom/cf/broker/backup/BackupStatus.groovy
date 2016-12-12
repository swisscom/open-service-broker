package com.swisscom.cf.broker.backup

import groovy.transform.CompileStatic

@CompileStatic
enum BackupStatus implements Serializable {

    CREATE_IN_PROGRESS("CREATE_IN_PROGRESS"),
    CREATE_SUCCEEDED("CREATE_SUCCEEDED"),
    CREATE_FAILED("CREATE_FAILED"),
    DELETE_IN_PROGRESS("DELETE_IN_PROGRESS"),
    DELETE_SUCCEEDED("DELETE_SUCCEEDED"),
    DELETE_FAILED("DELETE_FAILED")

    final String status

    BackupStatus(String status) { this.status = status }

    static BackupStatus of(String status) {
        return BackupStatus.values().find { it.status == status }
    }

    @Override
    public String toString() {
        return status;
    }
}
