package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.maltalex.ineter.base.IPAddress;
import com.github.maltalex.ineter.range.IPRange;
import com.github.maltalex.ineter.range.IPSubnet;
import com.github.maltalex.ineter.range.IPv4Subnet;
import com.swisscom.cloud.sb.broker.services.bosh.client.utils.json.LocalDateTimeDeserializer;
import org.apache.commons.lang3.tuple.Pair;
import org.immutables.value.Value;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfig.Network.Type.MANUAL;
import static com.swisscom.cloud.sb.broker.services.bosh.client.ImmutableBoshCloudConfig.of;
import static java.util.Collections.*;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

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
@JsonDeserialize(builder = BoshCloudConfig.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshCloudConfig {

    private static final Yaml YAML = new Yaml();
    public static final BoshCloudConfig EMPTY = of("",
                                                   "",
                                                   "",
                                                   emptySet(),
                                                   Compilation.EMPTY,
                                                   emptySet(),
                                                   emptySet(),
                                                   emptySet(),
                                                   "",
                                                   LocalDateTime.MIN,
                                                   false);

    @Value.Default
    public String getId() {
        return "";
    }

    public abstract String getName();

    @Value.Default
    public String getType() {
        return "cloud";
    }

    @Value.Default
    public Set<AvailabilityZone> getAvailabilityZones() {
        return emptySet();
    }

    /**
     * @see <a href='https://bosh.io/docs/azs/'>BOSH cloud config availability zones</a>
     */
    @Value.Immutable
    public static abstract class AvailabilityZone {

        public static final AvailabilityZone EMPTY = ImmutableAvailabilityZone.builder()
                                                                              .name("")
                                                                              .cpi("")
                                                                              .datacenter(Datacenter.EMPTY)
                                                                              .build();

        public abstract String getName();

        /**
         * NOT DOCUMENTED at BOSH site
         * <p>
         * Usually there is a general Cloud Provider Interface (CPI) configuration file that defines the underlying
         * cloud provider. This is the name of certain configuration done in that file.
         *
         * @return the name of a CPI defined on that BOSH instance
         */
        public abstract String getCpi();

        //TODO Not sure, I'm just adding this one because adding as cloud propety is a map of singleton of list of map of list... So not really generic
        public abstract Datacenter getDatacenter();

        @Value.Immutable
        public static abstract class Datacenter {

            public static final Datacenter EMPTY = ImmutableDatacenter.of("", "");

            public abstract String getName();

            public abstract String getCluster();
        }

        @Value.Default
        public Map<String, Object> getCloudProperties() {
            return emptyMap();
        }

        @Value.Derived
        public String getCloudPropertiesAsYaml() {
            return YAML.dumpAs(getCloudProperties(), Tag.MAP, BLOCK);
        }
    }

    @Value.Default
    public  Compilation getCompilation(){
        return Compilation.EMPTY;
    }

    @Value.Immutable
    public static abstract class Compilation {

        public static final Compilation EMPTY = ImmutableCompilation.builder()
                                                                    .network(Network.EMPTY)
                                                                    .availabilityZone("",
                                                                                      "",
                                                                                      ImmutableDatacenter.of("", ""),
                                                                                      emptyMap())
                                                                    .areCompilationVmsReused(false)
                                                                    .build();

        public abstract Network getNetwork();

        public abstract AvailabilityZone getAvailabilityZone();

        @Value.Default
        public boolean areCompilationVmsReused() {
            return true;
        }

        @Value.Default
        public int getNumberOfWorkers(){
            return 5;
        }

        public abstract Map<String, Object> getEnvironment();

        @Value.Derived
        public String getEnvironmentAsYaml() {
            return YAML.dumpAs(getEnvironment(), Tag.MAP, BLOCK);
        }

        public boolean isEmpty(){
            return EMPTY.equals(this);
        }

    }

    public abstract Set<VmType> getVmTypes();

    /**
     * @see <a href='https://bosh.io/docs/cloud-config/#vm-types'>BOSH Cloud Config Vm Types</a>
     */
    @Value.Immutable
    public static abstract class VmType {
        public abstract String getName();

        public abstract int getNumberOfCpus();

        public abstract int getRamSizeInMegabytes();

        public abstract int getDiskSizeInMegabytes();

        @Value.Default
        public String getInstanceType() {
            return "";
        }
    }

    public abstract Set<DiskType> getDiskTypes();

    /**
     * @see <a href='https://bosh.io/docs/cloud-config/#disk-types'>BOSH Cloud Config Disk Types</a>
     */
    @Value.Immutable
    public static abstract class DiskType {
        public abstract String getName();

        public abstract int getSizeInMegabytes();
    }

    public abstract Set<Network> getNetworks();

    /**
     * @see <a href='https://bosh.io/docs/cloud-config/#networks'>BOSH Cloud Config Network</a>
     */
    @Value.Immutable
    public static abstract class Network {
        public static final Network EMPTY = ImmutableNetwork.of("", Type.EMPTY, emptySet());

        public abstract String getName();

        @Value.Default
        public Type getType() {
            return MANUAL;
        }

        public enum Type {
            @JsonProperty("manual")
            MANUAL("manual"),
            @JsonProperty("dynamic")
            DYNAMIC("dynamic"),
            @JsonProperty("vip")
            VIP("vip"),
            EMPTY("");

            private final String value;

            Type(String value) {
                this.value = value;
            }

            public String value() {
                return value;
            }
        }

        public abstract Set<Subnet> getSubnets();

        @Value.Immutable
        public static abstract class Subnet {

            @Value.Default
            public String getName() {
                return "";
            }

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
            public List<IPRange> getStatic() {
                return emptyList();
            }

            @Value.Default
            public List<IPRange> getReserved() {
                return emptyList();
            }

            @Value.Default
            public Map<String, Object> getCloudProperties() {
                return emptyMap();
            }

            @Value.Derived
            public String getCloudPropertiesAsYaml() {
                return YAML.dumpAs(getCloudProperties(), Tag.MAP, BLOCK);
            }
        }
    }

    @Value.Default
    public String getContent() {
        return "";
    }

    @JsonProperty("created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Value.Default
    public LocalDateTime getCreatedAt() {
        return LocalDateTime.MIN;
    }

    @JsonProperty("current")
    @Value.Default
    public boolean isCurrent() {
        return false;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public static class Builder extends ImmutableBoshCloudConfig.Builder {

    }

    public static Builder cloudConfig() {
        return new BoshCloudConfig.Builder();
    }
}