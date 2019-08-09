package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(as = ImmutableBoshConfigResponse.class)
public interface BoshConfigResponse {
    int getId();

    String getName();

    String getType();

    String getContent();

    @JsonProperty("created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime getCreatedAt();

    boolean getCurrent();
}