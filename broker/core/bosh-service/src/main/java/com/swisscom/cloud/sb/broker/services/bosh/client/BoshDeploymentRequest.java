package com.swisscom.cloud.sb.broker.services.bosh.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.*;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(builder = ImmutableBoshDeploymentRequest.Builder.class)
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        overshadowImplementation = true,
        deepImmutablesDetection = true,
        depluralize = true,
        allParameters = true)
@Value.Immutable
public abstract class BoshDeploymentRequest {

    private static final Yaml YAML = new Yaml();

    public abstract String getName();

    @Value.Default
    public Set<BoshRelease> getReleases() {
        return emptySet();
    }

    @Value.Default
    public Set<BoshStemcell> getStemcells() {
        return emptySet();
    }

    @Value.Default
    public Set<InstanceGroup> getInstanceGroups() {
        return emptySet();
    }

    @Value.Default
    public Update getUpdate() {
        return Update.DEFAULT;
    }

    @Value.Default
    public Set<Variable> getVariables() {
        return emptySet();
    }

    @Value.Immutable
    public static abstract class InstanceGroup {

        public abstract String getName();

        @Value.Default
        public Map<String, Object> getEnvironmentProperties() {
            return emptyMap();
        }

        @Value.Derived
        public String getEnvironmentPropertiesAsYaml() {
            return YAML.dumpAs(getEnvironmentProperties(), Tag.MAP, BLOCK);
        }

        public abstract List<String> getAvailabilityZones();

        public abstract int getNumberOfInstances();

        public abstract String getVmType();

        public abstract BoshRelease getRelease();

        public abstract BoshStemcell getStemcell();

        public abstract List<String> getNetworks();

        public abstract List<Job> getJobs();

        @Value.Immutable
        public static abstract class Job {

            public abstract String getName();

            public abstract BoshRelease getRelease();

            //TODO Support more complex cases like the ones defined here https://bosh.io/docs/links/#overview
            @Value.Default
            public List<String> getConsumes() {
                return emptyList();
            }

            @Value.Default
            public List<String> getProvides() {
                return emptyList();
            }

            @Value.Default
            public Map<String, Object> getProperties() {
                return emptyMap();
            }

            @Value.Derived
            public String getPropertiesAsYaml() {
                return YAML.dumpAs(getProperties(), Tag.MAP, BLOCK);
            }

            public static class Builder extends ImmutableJob.Builder {

            }

            public static Job.Builder job() {
                return new Job.Builder();
            }
        }


        public static class Builder extends ImmutableInstanceGroup.Builder {

        }

        public static Builder instanceGroup() {
            return new InstanceGroup.Builder();
        }

    }

    @Value.Immutable
    public static abstract class Update {

        final static Update DEFAULT = update().numberOfCanaries(1)
                                              .maxInFlight(1)
                                              .serial(false)
                                              .canaryWatchTime("1000-60000")
                                              .updateWatchTime("1000-60000")
                                              .build();

        public abstract int getNumberOfCanaries();

        public abstract int getMaxInFlight();

        public abstract boolean getSerial();

        public abstract String getCanaryWatchTime();

        public abstract String getUpdateWatchTime();

        public static class Builder extends ImmutableUpdate.Builder {

        }

        public static Update.Builder update() {
            return new Update.Builder();
        }

    }

    @Value.Immutable
    public static abstract class Variable {

        public abstract String getName();

        public abstract Map<String, Object> getOptions();

        public abstract Type getType();

        public enum Type {
            PASSWORD("password"),
            CERTIFICATE("certificate"),
            CERTIFICATE_CA("certificate"),
            RSA("rsa"),
            SSH("ssh");

            private final String value;

            Type(String value) {
                this.value = value;
            }

            public String value() {
                return value;
            }
        }

        public static class Builder extends ImmutableVariable.Builder {

        }

        public static Variable.Builder variable() {
            return new Variable.Builder();
        }

    }

    public static class Builder extends ImmutableBoshDeploymentRequest.Builder {

    }

    public static BoshDeploymentRequest.Builder deploymentRequest() {
        return new BoshDeploymentRequest.Builder();
    }
}
