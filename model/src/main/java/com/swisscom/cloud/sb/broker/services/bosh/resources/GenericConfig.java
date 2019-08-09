package com.swisscom.cloud.sb.broker.services.bosh.resources;

import org.immutables.value.Value;

@Value.Immutable
public interface GenericConfig {
    String getTemplateName();
    String getType();

    static ImmutableGenericConfig.Builder genericConfig() {
        return ImmutableGenericConfig.builder();
    }
}