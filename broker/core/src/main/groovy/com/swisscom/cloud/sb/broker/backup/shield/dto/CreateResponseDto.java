package com.swisscom.cloud.sb.broker.backup.shield.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableCreateResponseDto.class)
public abstract class CreateResponseDto {
    public abstract String getOk();

    @JsonProperty("uuid")
    public abstract String getUuid();
}
