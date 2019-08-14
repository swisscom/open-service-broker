package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(builder = "new")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = ImmutableBoshInfo.Builder.class)
public interface BoshInfo {
    String getName();

    String getUuid();

    @JsonProperty("user_authentication")
    BoshUserAuthentication getUserAuthentication();
}
