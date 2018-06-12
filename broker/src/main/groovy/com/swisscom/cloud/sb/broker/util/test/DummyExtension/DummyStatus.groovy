package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.cfextensions.extensions.Status

enum DummyStatus implements Status {
    INIT('initialized', false),
    IN_PROGRESS('in_progress', false),
    SUCCESS('success', true),
    FAILED('failed', true)

    final String status
    final boolean isFinalState

    DummyStatus(String status, boolean isFinalState) { this.status = status; this.isFinalState = isFinalState }

    @Override
    String toString() {
        return status
    }
}