package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("queued")
        QUEUED("queued", false),
        @JsonProperty("processing")
        PROCESSING("processing", false),
        @JsonProperty("cancelled")
        CANCELLED("cancelled", false),
        @JsonProperty("cancelling")
        CANCELLING("cancelling", false),
        @JsonProperty("done")
        DONE("done", true),
        @JsonProperty("errored")
        ERRORED("errored", false),
        @JsonProperty("error")
        ERROR("error", false),
        @JsonProperty("")
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

        public static Optional<State> fromString(String symbol) {
            return Optional.ofNullable(stringToEnum.get(symbol));
        }


    }
}
