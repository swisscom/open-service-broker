package com.swisscom.cf.broker.backup.shield.dto

enum JobStatus {
    FAILED("failed"), FINISHED("finished"), RUNNING("running")

    final String status

    JobStatus(final String status) {
        this.status = status
    }
}
