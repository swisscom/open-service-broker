package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(as = ImmutableTask.class)
public interface Task {
    int getId();

    State getState();

    String getDescription();

    int getTimestamp();

    @Value.Default
    default String getResult() {
        return "";
    }

    String getUser();

    enum State {
        queued, processing, cancelled, cancelling, done, errored, error
    }
}
