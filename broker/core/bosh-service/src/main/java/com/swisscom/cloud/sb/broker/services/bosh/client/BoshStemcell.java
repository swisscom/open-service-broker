package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = BoshStemcell.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshStemcell {

    public abstract UUID getCid();

    public abstract String getName();

    @JsonProperty("operating_system")
    public abstract String getOperatingSystem();

    public abstract String getVersion();

    @JsonProperty("api_version")
    public abstract String getApiVersion();

    public abstract String getCpi();

    public abstract Collection<DeploymentName> getDeployments();

    @JsonInclude(Include.NON_NULL)
    @JsonDeserialize(builder = ImmutableDeploymentName.Builder.class)
    @Value.Immutable
    public static abstract class DeploymentName {

        public abstract String getName();
    }

    public static class Builder extends ImmutableBoshStemcell.Builder {

    }

    public static BoshStemcell.Builder stemcell() {
        return new BoshStemcell.Builder();
    }
}
