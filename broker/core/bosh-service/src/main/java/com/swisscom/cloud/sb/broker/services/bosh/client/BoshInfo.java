package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshUserAuthentication.EMPTY;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = BoshInfo.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshInfo {

    @Value.Default
    public String getName() {
        return "";
    }

    public abstract String getUuid();

    @Value.Default
    public String getVersion() {
        return "";
    }

    @JsonProperty("user_authentication")
    @Value.Default
    public BoshUserAuthentication getUserAuthentication() {
        return EMPTY;
    }

    public static class Builder extends ImmutableBoshInfo.Builder {

    }

    public static BoshInfo.Builder info() {
        return new BoshInfo.Builder();
    }
}
