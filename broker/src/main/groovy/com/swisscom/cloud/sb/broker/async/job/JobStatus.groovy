package com.swisscom.cloud.sb.broker.async.job

enum JobStatus {
    FAILED("failed"), SUCCESSFUL("successful"), RUNNING("running")

    final String status

    JobStatus(final String status) {
        this.status = status
    }
}
