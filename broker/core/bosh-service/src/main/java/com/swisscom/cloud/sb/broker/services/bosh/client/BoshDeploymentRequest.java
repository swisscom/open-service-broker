package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.UUID;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = ImmutableBoshDeploymentRequest.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshDeploymentRequest {

    public final static UUID NIL = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Value.Default
    public String getName() {
        return "";
    }

    @Value.Default
    public String getYamlContent() {
        return "";
    }

    public static class Builder extends ImmutableBoshDeploymentRequest.Builder {

    }

    public static Builder deploymentRequest() {
        return new BoshDeploymentRequest.Builder();
    }
}
