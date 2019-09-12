package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.Map;

import static com.swisscom.cloud.sb.broker.services.bosh.client.ImmutableBoshUserAuthentication.of;

@JsonDeserialize(builder = ImmutableBoshUserAuthentication.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshUserAuthentication {

    public static final BoshUserAuthentication EMPTY = of("", Collections.emptyMap());

    public abstract String getType();

    public abstract Map<String, Object> getOptions();

    public String getUrl() {
        return String.valueOf(getOptions().get("url"));
    }
}
