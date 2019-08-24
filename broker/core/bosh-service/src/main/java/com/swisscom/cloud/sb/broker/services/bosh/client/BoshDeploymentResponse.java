package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.StringJoiner;

import static java.util.Collections.emptyList;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = BoshDeploymentResponse.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshDeploymentResponse {

    @Value.Default
    public String getName() {
        return "";
    }

    @Value.Default
    public Collection<String> getTeams() {
        return emptyList();
    }

    @JsonProperty("cloud_config")
    @Value.Default
    public String getCloudConfig() {
        return "none";
    }

    @Value.Default
    public Collection<VersionedName> getStemcells() {
        return emptyList();
    }

    @Value.Default
    public Collection<VersionedName> getReleases() {
        return emptyList();
    }

    @JsonDeserialize(builder = ImmutableVersionedName.Builder.class)
    @Value.Immutable
    public static abstract class VersionedName{

        public abstract String getName();

        public abstract String getVersion();

        @Override
        public String toString() {
            return new StringJoiner(" ",  "[", "]")
                    .add(getName())
                    .add("v" + getVersion())
                    .toString();
        }
    }

    @Value.Default
    public URI getTaskUri() {
        return URI.create("");
    }

    @Value.Default
    @Nullable
    public String getManifest() {
        return "";
    }

    @Value.Lazy
    public String getTaskId() {
        return getTaskUri().getPath().substring(getTaskUri().getPath().lastIndexOf("/") + 1);
    }

    public static class Builder extends ImmutableBoshDeploymentResponse.Builder {

    }

    public static Builder deploymentResponse() {
        return new BoshDeploymentResponse.Builder();
    }


}
