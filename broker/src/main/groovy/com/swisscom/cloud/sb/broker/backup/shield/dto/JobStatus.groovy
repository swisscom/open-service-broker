package com.swisscom.cloud.sb.broker.backup.shield.dto

enum JobStatus {
    FAILED("failed"), SUCCESSFUL("successful"), RUNNING("running")

    final String status

    JobStatus(final String status) {
        this.status = status
    }
}
