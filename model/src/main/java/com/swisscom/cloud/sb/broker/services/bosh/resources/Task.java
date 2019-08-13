package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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
        QUEUED("queued", false),
        PROCESSING("processing", false),
        CANCELLED("cancelled", false),
        CANCELLING("cancelled", false),
        DONE("done", true),
        ERRORED("errored", false),
        ERROR("error", false),
        UNKNOWN("", false);

        private final String value;
        private final boolean successful;

        State(String value, boolean successful) {
            this.value = value;
            this.successful = successful;
        }

        public String value() {
            return value;
        }

        public boolean isSuccessful() {
            return successful;
        }

        private static final Map<String, State> stringToEnum = Stream.of(values())
                                                                     .collect(toMap(Object::toString, e -> e));

        @JsonCreator
        public static Optional<State> fromString(String symbol) {
            return Optional.ofNullable(stringToEnum.get(symbol));
        }


    }
}
