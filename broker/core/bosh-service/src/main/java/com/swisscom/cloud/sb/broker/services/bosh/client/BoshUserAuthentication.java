package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;

@JsonDeserialize(builder = ImmutableBoshUserAuthentication.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshUserAuthentication {

    public abstract String getType();

    public abstract Map<String, Object> getOptions();

    public String getUrl(){
        return String.valueOf(getOptions().get("url"));
    }
}
