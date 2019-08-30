package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.swisscom.cloud.sb.broker.services.bosh.client.utils.json.LocalDateTimeDeserializer;
import org.immutables.value.Value;

import java.time.LocalDateTime;

import static com.swisscom.cloud.sb.broker.services.bosh.client.ImmutableBoshCloudConfig.of;

/**
 * Represents a <em>BOSH Cloud Config</em>
 * <p>
 * Previously each deployment manifest specified IaaS and IaaS agnostic configuration in a single file. As more
 * deployments are managed by the Director, it becomes inconvenient to keep shared IaaS configuration in sync in all
 * deployment manifests. In addition, multiple deployments typically want to use the same network subnet, hence IP
 * ranges need to be separated and reserved.
 * </p>
 * <p>
 * The cloud config is a YAML file that defines IaaS specific configuration used by the Director and all deployments. It
 * allows us to separate IaaS specific configuration into its own file and keep deployment manifests IaaS agnostic.
 * </p>
 *
 * @see <a href='https://bosh.io/docs/cloud-config/'>BOSH Cloud Config</a>
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ImmutableBoshCloudConfig.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshCloudConfig {

    public static final BoshCloudConfig EMPTY = of("", "", "", "", LocalDateTime.MIN, false);

    public abstract String getId();

    public abstract String getName();

    public abstract String getType();

    public abstract String getContent();

    @JsonProperty("created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public abstract LocalDateTime getCreatedAt();

    public abstract boolean getCurrent();

    public boolean isEmpty(){
        return this.equals(EMPTY);
    }
}