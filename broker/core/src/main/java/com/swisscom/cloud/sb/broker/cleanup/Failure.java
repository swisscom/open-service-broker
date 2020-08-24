package com.swisscom.cloud.sb.broker.cleanup;

import org.immutables.value.Value;

@Value.Immutable
public interface Failure {
    static Builder builder() {
        return new Builder();
    }

    String message();

    String description();

    Throwable exception();

    class Builder extends ImmutableFailure.Builder {
    }
}
