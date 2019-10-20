package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.StringUtils;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDirectorTask.Event.State.UNKNOWN;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

/**
 * The basic unit of work performed by the Director. You can get the status and logs for any task. You can monitor the
 * task throughout its lifecycle, which progresses through states like queued, processing, done, and error.
 *
 * @see <a href='https://bosh.io/docs/director-api-v1/#tasks'>Director API v1.0.0: Tasks</a>
 * @see <a href='https://bosh.io/docs/director-tasks/'>BOSH Documentation: Reviewing Tasks</a>
 */
@JsonDeserialize(builder = BoshDirectorTask.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshDirectorTask {

    public static final BoshDirectorTask EMPTY = BoshDirectorTask.boshTask()
                                                                 .id("")
                                                                 .state(State.UNKNOWN)
                                                                 .description("")
                                                                 .timestamp(-1L)
                                                                 .user("")
                                                                 .build();

    public abstract String getId();

    public abstract State getState();

    public abstract String getDescription();

    @Value.Default
    @Nullable
    public String getDeployment() {
        return "";
    }

    @JsonProperty("context_id")
    @Value.Default
    public String getContextId() {
        return "";
    }

    public abstract long getTimestamp();

    @Value.Default
    @Nullable
    public String getResult() {
        return "";
    }

    public abstract String getUser();

    @JsonProperty("started_at")
    @Value.Default
    @Nullable
    public Long getStartedAt() {
        return -1L;
    }

    @Value.Default
    public Collection<Event> getEvents() {
        return emptyList();
    }

    @Value.Derived
    public Event lastEvent() {
        if (getEvents().size() == 0) {
            return Event.EMPTY;
        }
        Iterator<Event> iterator = getEvents().iterator();
        Event result;
        do {
            result = iterator.next();
        } while (iterator.hasNext());
        return result;
    }

    @Value.Lazy
    public String getEventsAsJson() {
        try {
            return new ObjectMapper().writeValueAsString(getEvents());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Can't convert events to JSON", e);
        }
    }

    public enum State {
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

    @JsonDeserialize(builder = ImmutableEvent.Builder.class)
    @Value.Immutable
    public static abstract class Event implements Comparable<Event> {
        public static final Event EMPTY = ImmutableEvent.of(-1L,
                                                            0,
                                                            0,
                                                            State.UNKNOWN,
                                                            0,
                                                            "",
                                                            emptyMap(),
                                                            Error.EMPTY,
                                                            "",
                                                            emptyList());

        public abstract long getTime();

        @Value.Default
        public int getIndex() {
            return -1;
        }

        @Value.Default
        public int getTotal() {
            return 11111;
        }

        @Value.Default
        public State getState() {
            return UNKNOWN;
        }

        public enum State {
            @JsonProperty("started")
            STARTED("started"),
            @JsonProperty("finished")
            FINISHED("finished"),
            @JsonProperty("failed")
            FAILED("failed"),
            @JsonProperty("cancelled")
            CANCELLED("cancelled"),
            @JsonProperty("unknown")
            UNKNOWN("unknown");

            private final String value;

            State(String value) {
                this.value = value;
            }

            public String value() {
                return value;
            }
        }

        @Value.Default
        public int getProgress() {
            return 0;
        }

        @Value.Default
        public String getTask() {
            return "";
        }

        @Value.Default
        @Nullable
        public Map<String, String> getData() {
            return emptyMap();
        }

        @Value.Default
        @Nullable
        public Error getError() {
            return Error.EMPTY;
        }

        @JsonDeserialize(builder = ImmutableError.Builder.class)
        @Value.Immutable
        public static abstract class Error {

            static final Error EMPTY = ImmutableError.of(-1, "");

            public abstract int getCode();

            public abstract String getMessage();
        }

        @Value.Default
        @Nullable
        public String getStage() {
            return "";
        }

        @Value.Default
        @Nullable
        public Collection<String> getTags() {
            return emptyList();
        }

        @Override
        public String toString() {
            if (hasError()) {
                return new StringJoiner(" ", Event.class.getSimpleName() + "[", "]")
                        .add(valueOf(getTime()))
                        .add(valueOf(getError().getCode()))
                        .add(getError().getMessage())
                        .toString();
            }
            return new StringJoiner(" ", Event.class.getSimpleName() + "[", "]")
                    .add(valueOf(getTime()))
                    .add(valueOf(getIndex()))
                    .add("<" + getStage() + ">")
                    .add("-> " + getTask())
                    .add(getState().name())
                    .toString();

        }

        public boolean hasError() {
            return !Objects.equals(getError(), Error.EMPTY);
        }

        @Override
        public int compareTo(Event o) {
            return Comparator.comparing(Event::getTime)
                             .thenComparing(Event::getTotal)
                             .thenComparing(Event::getIndex)
                             .thenComparing(Event::getProgress)
                             .compare(this, o);
        }
    }

    public static class Builder extends ImmutableBoshDirectorTask.Builder {

    }

    public static Builder boshTask() {
        return new BoshDirectorTask.Builder();
    }

    @Override
    public String toString() {
        StringJoiner join = new StringJoiner(" ", BoshDirectorTask.class.getSimpleName() + "[", "]")
                .add(valueOf(getTimestamp()))
                .add(getId())
                .add("'" + getDescription() + "'")
                .add("for '" + getDeployment() + "'")
                .add(getState().toString())
                .add("=> " + getResult());
        if (!getEvents().isEmpty()) {
            join.add("\n\t|-")
                .add(StringUtils.join(getEvents(), "\n\t|- "));
        }
        return join.toString();
    }
}
