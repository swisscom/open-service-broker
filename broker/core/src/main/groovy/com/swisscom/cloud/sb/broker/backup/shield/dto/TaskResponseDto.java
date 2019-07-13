package com.swisscom.cloud.sb.broker.backup.shield.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@JsonDeserialize(as = ImmutableTaskResponseDto.class)
public abstract class TaskResponseDto {
    public abstract String getOk();

    @JsonProperty("task_uuid")
    public abstract UUID getTaskUuid();
}
