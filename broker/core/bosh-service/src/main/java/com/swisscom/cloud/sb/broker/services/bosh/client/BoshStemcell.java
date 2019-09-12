package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.UUID;

import static java.util.Collections.emptyList;

/**
 * Represents a <em>BOSH Stemcell</em>
 * <p>A generic VM image that BOSH clones and configures during deployment. A stemcell is a template from which BOSH
 * creates whatever VMs are needed for a wide variety of components and products.</p>
 *
 * @see <a href='https://bosh.io/docs/director-api-v1/#stemcells'>BOSH Director API v1.0.0: Stemcells</a>
 * @see <a href='https://bosh.io/docs/stemcell/'>What is a Stemcell?</a>
 */
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

    @Value.Default
    public String getCid() {
        return "";
    }

    public abstract String getName();

    @JsonProperty("operating_system")
    @Value.Default
    public String getOperatingSystem() {
        return "";
    }

    @Value.Default
    public String getVersion() {
        return "";
    }

    /**
     * Don't look at me: BOSH had the idea that this field is an integer.
     * @return the version of the BOSH API?
     */
    @JsonProperty("api_version")
    @Value.Default
    public int getApiVersion() {
        return 2;
    }

    @Value.Default
    public String getCpi() {
        return "";
    }

    @Value.Default
    public Collection<DeploymentName> getDeployments() {
        return emptyList();
    }

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
