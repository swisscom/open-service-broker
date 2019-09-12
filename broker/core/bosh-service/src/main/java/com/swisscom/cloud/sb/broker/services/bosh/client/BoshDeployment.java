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

/**
 * An encapsulation of software and configuration that BOSH can deploy to the cloud. You can think of a deployment as
 * the state of a collection of VMs: what software is on them, what resources they use, and how these are orchestrated.
 * Even though BOSH creates the deployment using ephemeral resources, the deployment is stable in that BOSH re-creates
 * VMs that fail and otherwise works to keep your software running. BOSH also manages persistent disks so that state
 * (for example, database data files) can survive when VMs are re-created. Combination of a deployment manifest,
 * stemcells, and releases is portable across different clouds with minimal changes to the deployment manifest.
 *
 * @see <a href='https://bosh.io/docs/director-api-v1/#deployments'>Director API v1.0.0: Deployments</a>
 * @see <a href='https://bosh.io/docs/deployment/'>What is a Deployment?</a>
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = BoshDeployment.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshDeployment {

    public static final BoshDeployment EMPTY = boshDeployment().build();

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
    public static abstract class VersionedName {

        public abstract String getName();

        public abstract String getVersion();

        @Override
        public String toString() {
            return new StringJoiner(" ", "[", "]")
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

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public static class Builder extends ImmutableBoshDeployment.Builder {


    }

    public static Builder boshDeployment() {
        return new BoshDeployment.Builder();
    }


}
