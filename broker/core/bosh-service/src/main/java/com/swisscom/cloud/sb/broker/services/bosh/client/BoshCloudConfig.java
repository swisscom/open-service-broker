package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.maltalex.ineter.base.IPAddress;
import com.github.maltalex.ineter.range.IPSubnet;
import com.github.maltalex.ineter.range.IPv4Subnet;
import com.swisscom.cloud.sb.broker.services.bosh.client.utils.json.LocalDateTimeDeserializer;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.swisscom.cloud.sb.broker.services.bosh.client.ImmutableBoshCloudConfig.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

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

    public static final BoshCloudConfig EMPTY = of("",
                                                   "",
                                                   "",
                                                   emptySet(),
                                                   emptySet(),
                                                   emptySet(),
                                                   "",
                                                   LocalDateTime.MIN,
                                                   false);

    public abstract String getId();

    public abstract String getName();

    public abstract String getType();

    public abstract Set<VmType> getVmTypes();

    @Value.Immutable
    public static abstract class VmType {
        public abstract String getName();
    }

    public abstract Set<DiskType> getDiskTypes();

    @Value.Immutable
    public static abstract class DiskType {
        public abstract String getName();

        public abstract int getSize();

        //TODO PRetty sure that this disk type has much more properties
    }

    public abstract Set<Network> getNetworks();

    @Value.Immutable
    public static abstract class Network {
        public abstract String getName();

        //TODO create enum Type
        public abstract String getType();

        public abstract Set<Subnet> getSubnets();

        @Value.Immutable
        public static abstract class Subnet {

            @Value.Default
            public List<String> getAvailabilityZones() {
                return emptyList();
            }

            @Value.Default
            public List<IPAddress> getDns() {
                return emptyList();
            }

            @Value.Default
            public IPSubnet getRange() {
                return IPv4Subnet.of("0.0.0.0/0");
            }

            @Value.Default
            public IPAddress getGateway() {
                return IPAddress.of("0.0.0.0");
            }

            //FIXME It could be also an IPRange See https://bosh.io/docs/networks/#automatic-ip-assignment
            @Value.Default
            public List<IPAddress> getStatic() {
                return emptyList();
            }

            @Value.Default
            public List<IPSubnet> getReserved() {
                return emptyList();
            }
        }
    }

    public abstract String getContent();

    @JsonProperty("created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public abstract LocalDateTime getCreatedAt();

    @JsonProperty("current")
    public abstract boolean isCurrent();

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}