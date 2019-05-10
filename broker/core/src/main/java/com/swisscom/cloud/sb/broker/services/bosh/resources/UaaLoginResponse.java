package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(builder = "new")
@JsonDeserialize(builder = ImmutableUaaLoginResponse.Builder.class)
public interface UaaLoginResponse {
    @JsonProperty("access_token")
    String getAccessToken();
}
