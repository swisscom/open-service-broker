package com.swisscom.cloud.sb.broker.services.bosh.resources;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
@Value.Style(builder = "new")
@JsonDeserialize(builder = ImmutableBoshUserAuthentication.Builder.class)
public interface BoshUserAuthentication {
    String getType();

    Map<String, Object> getOptions();
}
