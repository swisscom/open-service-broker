package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.Collections;

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshRelease.Version.LATEST;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

/**
 * Represents a <em>BOSH Release</em>
 * <p>
 * A collection of configuration files, source code, jobs, packages and accompanying information needed to make a
 * software component deployable by BOSH. A self-contained release should have no dependencies that need to be fetched
 * from the internet.
 * </p>
 *
 * @see <a href='https://bosh.io/docs/director-api-v1/#releases'>BOSH Director API v1.0.0: BOSH Releases</a>
 * @see <a href='https://bosh.io/docs/release/'>What is a Release?</a>
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = BoshRelease.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshRelease {

    public abstract String getName();

    @JsonProperty("release_versions")
    @Value.Default
    public  Collection<Version> getReleaseVersions(){
        return emptyList();
    }

    @Value.Lazy
    public Version getLatest() {
        if (getReleaseVersions().isEmpty()) {
            return LATEST;
        } else {
            return getReleaseVersions().stream()
                                       .max(comparing(Version::getVersion))
                                       .get();
        }
    }


    @JsonInclude(Include.NON_NULL)
    @JsonDeserialize(builder = ImmutableVersion.Builder.class)
    @Value.Immutable
    public static abstract class Version {

        public static final Version LATEST = ImmutableVersion.of("latest", "", false, false, emptyList());

        public abstract String getVersion();

        @JsonProperty("commit_hash")
        public abstract String getCommitHash();

        @JsonProperty("uncommitted_changes")
        public abstract boolean hasUncommittedChanges();

        @JsonProperty("currently_deployed")
        public abstract boolean isCurrentlyDeployed();

        @JsonProperty("job_names")
        public abstract Collection<String> getJobNames();
    }

    public static class Builder extends ImmutableBoshRelease.Builder {

    }

    public static BoshRelease.Builder release() {
        return new BoshRelease.Builder();
    }
}
