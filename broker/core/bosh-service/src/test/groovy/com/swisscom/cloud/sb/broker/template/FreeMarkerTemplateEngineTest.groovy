package com.swisscom.cloud.sb.broker.template

import com.google.common.collect.ImmutableMap
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshInfo
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshRelease
import com.swisscom.cloud.sb.broker.services.bosh.client.BoshStemcell
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.InstanceGroup.Job.job
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.InstanceGroup.instanceGroup
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.Variable.Type.*
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.Variable.variable
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.deploymentRequest
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshRelease.release
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshStemcell.stemcell
import static java.util.UUID.randomUUID

class FreeMarkerTemplateEngineTest extends Specification {

    private final static Logger LOGGER = LoggerFactory.getLogger(FreeMarkerTemplateEngine.class)

    private static final String MINIMAL = "bosh-deployment-minimal.ftlh"
    private static final String COMPLETE = "bosh-deployment.ftlh"

    private static final BoshRelease SIMPLE_RELEASE = release().name("release").build()
    private static final BoshStemcell SIMPLE_STEMCELL = stemcell().
            name("stemcell").
            operatingSystem("os").
            version("1.0.0").
            build()

    @Shared
    TemplateEngine templateEngine

    @Shared
    BoshDeploymentRequest.InstanceGroup.Builder simpleInstanceGroup = instanceGroup().
            name("instanceGroup").
            numberOfInstances(1).
            putEnvironmentProperty("persistent_disk_mount_options", ["noatime"]).
            addAvailabilityZone("az1").
            vmType("vmType").
            addNetwork("network").
            addJob(job().name("job").release(release().name("release").build()).build())

    def setupSpec() {
        templateEngine = FreeMarkerTemplateEngine.of(Paths.get("src", "main", "resources", "templates").toFile())
    }

    @Unroll
    def "process '#templateId' BOSH deployment with #request"() {
        given:
        Map<String, Object> modelMap = ImmutableMap.builder().
                put("deploy", request).
                put("bosh_prefix", prefix).
                put("bosh_info", BoshInfo.info().uuid(boshId).build()).
                build()

        when:
        String result = templateEngine.process(templateId, modelMap)

        then:
        result != null
        result == expected
        LOGGER.debug("Result:\n" + result)

        where:
        templateId | serviceInstanceId  | prefix | boshId             | release        | stemcell
        MINIMAL    | randomUUIDString() | "d-"   | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        MINIMAL    | randomUUIDString() | "d-"   | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        MINIMAL    | randomUUIDString() | "d-"   | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        MINIMAL    | randomUUIDString() | "d-"   | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        COMPLETE   | randomUUIDString() | "d"    | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        COMPLETE   | randomUUIDString() | "d"    | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        COMPLETE   | randomUUIDString() | "d"    | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        COMPLETE   | randomUUIDString() | "d"    | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        COMPLETE   | randomUUIDString() | "d"    | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL
        COMPLETE   | randomUUIDString() | "d"    | randomUUIDString() | SIMPLE_RELEASE | SIMPLE_STEMCELL

        instanceGroup << [
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).
                        stemcell(SIMPLE_STEMCELL).
                        addJob("complex-job",
                               SIMPLE_RELEASE,
                               ["consumes-a", "consumes-b"],
                               ["provides-a", "provides-b"],
                               Collections.emptyMap()).
                        build(),
                simpleInstanceGroup.release(SIMPLE_RELEASE).
                        stemcell(SIMPLE_STEMCELL).
                        addJob("complex-job",
                               SIMPLE_RELEASE,
                               ["consumes-a", "consumes-b"],
                               ["provides-a", "provides-b"],
                               ImmutableMap.<String, Object> builder()
                                           .put("string", "string")
                                           .put("list", ["list1", "list2", "list3"])
                                           .put("map", ["map1": "value",
                                                        "map2": ["value1", "value2"],
                                                        "map3": ["map31": "value", "map32": "value"]])
                                           .build()).
                        build(),

                simpleInstanceGroup.release(SIMPLE_RELEASE).
                        stemcell(SIMPLE_STEMCELL).
                        addJob("complex-job",
                               SIMPLE_RELEASE,
                               ["consumes-a", "consumes-b"],
                               ["provides-a", "provides-b"],
                               ImmutableMap.<String, Object> builder()
                                           .put("string", "string")
                                           .put("list", ["list1", "list2", "list3"])
                                           .put("map", ["map1": "value",
                                                        "map2": ["value1", "value2"],
                                                        "map3": ["map31": "value", "map32": "value"]])
                                           .build()).
                        build()
        ]

        request << [
                deploymentRequest().
                        name(serviceInstanceId).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        addInstanceGroup(instanceGroup).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        addInstanceGroup(instanceGroup).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        addInstanceGroup(instanceGroup).
                        addVariable(variable().
                                name("password").
                                type(PASSWORD).
                                putOption("length", 40).
                                build()).
                        addVariable(variable().
                                name("ca").
                                type(CERTIFICATE_CA).
                                putOption("common_name", "ca").
                                putOption("is_ca", "true").
                                build()).
                        addVariable(variable().
                                name("certificate").
                                type(CERTIFICATE).
                                putOption("common_name", "certificate").
                                putOption("ca", "ca").
                                build()).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        addInstanceGroup(instanceGroup).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        addInstanceGroup(instanceGroup).
                        build(),
                deploymentRequest().
                        name(serviceInstanceId).
                        addRelease(release).
                        addStemcell(stemcell).
                        addInstanceGroup(instanceGroup).
                        addVariable(variable().
                                name("password").
                                type(PASSWORD).
                                putOption("length", 40).
                                build()).
                        addVariable(variable().
                                name("ca").
                                type(CERTIFICATE_CA).
                                putOption("common_name", "ca").
                                putOption("is_ca", "true").
                                build()).
                        addVariable(variable().
                                name("certificate").
                                type(CERTIFICATE).
                                putOption("common_name", "certificate").
                                putOption("ca", "ca").
                                build()).
                        build()

        ]

        expected << ["name: \"d-$serviceInstanceId\"\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000",

                     "name: \"d-$serviceInstanceId\"\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000",

                     "name: \"d-$serviceInstanceId\"\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000",

                     "name: \"d-$serviceInstanceId\"\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "instance_groups:\n" +
                     "  - name: instanceGroup\n" +
                     "    azs: [az1]\n" +
                     "    instances: 1\n" +
                     "    vm_type: vmType\n" +
                     "    release: release\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: \"network\"\n" +
                     "    jobs:\n" +
                     "      - name: job\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000",

                     "name: \"$prefix-$serviceInstanceId\"\n" +
                     "director_uuid: '$boshId'\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000\n",

                     "name: \"$prefix-$serviceInstanceId\"\n" +
                     "director_uuid: '$boshId'\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "instance_groups:\n" +
                     "  - name: instanceGroup\n" +
                     "    azs: [az1]\n" +
                     "    env:\n" +
                     "      persistent_disk_mount_options:\n" +
                     "      - noatime\n" +
                     "    instances: 1\n" +
                     "    vm_type: vmType\n" +
                     "    release: release\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: \"network\"\n" +
                     "    jobs:\n" +
                     "      - name: job\n" +
                     "        release: release\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000\n",

                     "name: \"$prefix-$serviceInstanceId\"\n" +
                     "director_uuid: '$boshId'\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "instance_groups:\n" +
                     "  - name: instanceGroup\n" +
                     "    azs: [az1]\n" +
                     "    env:\n" +
                     "      persistent_disk_mount_options:\n" +
                     "      - noatime\n" +
                     "    instances: 1\n" +
                     "    vm_type: vmType\n" +
                     "    release: release\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: \"network\"\n" +
                     "    jobs:\n" +
                     "      - name: job\n" +
                     "        release: release\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000\n" +
                     "variables:\n" +
                     "    - name: password\n" +
                     "      type: password\n" +
                     "      options:\n" +
                     "        length: 40\n" +
                     "    - name: ca\n" +
                     "      type: certificate\n" +
                     "      options:\n" +
                     "        common_name: ca\n" +
                     "        is_ca: true\n" +
                     "    - name: certificate\n" +
                     "      type: certificate\n" +
                     "      options:\n" +
                     "        common_name: certificate\n" +
                     "        ca: ca\n",

                     "name: \"$prefix-$serviceInstanceId\"\n" +
                     "director_uuid: '$boshId'\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "instance_groups:\n" +
                     "  - name: instanceGroup\n" +
                     "    azs: [az1]\n" +
                     "    env:\n" +
                     "      persistent_disk_mount_options:\n" +
                     "      - noatime\n" +
                     "    instances: 1\n" +
                     "    vm_type: vmType\n" +
                     "    release: release\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: \"network\"\n" +
                     "    jobs:\n" +
                     "      - name: job\n" +
                     "        release: release\n" +
                     "      - name: complex-job\n" +
                     "        release: release\n" +
                     "        consumes:\n" +
                     "          consumes-a:\n" +
                     "            from: consumes-a\n" +
                     "          consumes-b:\n" +
                     "            from: consumes-b\n" +
                     "        provides:\n" +
                     "          provides-a:\n" +
                     "            as: provides-a\n" +
                     "          provides-b:\n" +
                     "            as: provides-b\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000\n",

                     "name: \"$prefix-$serviceInstanceId\"\n" +
                     "director_uuid: '$boshId'\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "instance_groups:\n" +
                     "  - name: instanceGroup\n" +
                     "    azs: [az1]\n" +
                     "    env:\n" +
                     "      persistent_disk_mount_options:\n" +
                     "      - noatime\n" +
                     "    instances: 1\n" +
                     "    vm_type: vmType\n" +
                     "    release: release\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: \"network\"\n" +
                     "    jobs:\n" +
                     "      - name: job\n" +
                     "        release: release\n" +
                     "      - name: complex-job\n" +
                     "        release: release\n" +
                     "        consumes:\n" +
                     "          consumes-a:\n" +
                     "            from: consumes-a\n" +
                     "          consumes-b:\n" +
                     "            from: consumes-b\n" +
                     "        provides:\n" +
                     "          provides-a:\n" +
                     "            as: provides-a\n" +
                     "          provides-b:\n" +
                     "            as: provides-b\n" +
                     "      - name: complex-job\n" +
                     "        release: release\n" +
                     "        consumes:\n" +
                     "          consumes-a:\n" +
                     "            from: consumes-a\n" +
                     "          consumes-b:\n" +
                     "            from: consumes-b\n" +
                     "        provides:\n" +
                     "          provides-a:\n" +
                     "            as: provides-a\n" +
                     "          provides-b:\n" +
                     "            as: provides-b\n" +
                     "        properties:\n" +
                     "          string: string\n" +
                     "          list:\n" +
                     "          - list1\n" +
                     "          - list2\n" +
                     "          - list3\n" +
                     "          map:\n" +
                     "            map1: value\n" +
                     "            map2:\n" +
                     "            - value1\n" +
                     "            - value2\n" +
                     "            map3:\n" +
                     "              map31: value\n" +
                     "              map32: value\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000\n",

                     "name: \"$prefix-$serviceInstanceId\"\n" +
                     "director_uuid: '$boshId'\n" +
                     "releases:\n" +
                     "  - name: release\n" +
                     "    version: latest\n" +
                     "stemcells:\n" +
                     "  - alias: stemcell\n" +
                     "    os: os\n" +
                     "    version: 1.0.0\n" +
                     "instance_groups:\n" +
                     "  - name: instanceGroup\n" +
                     "    azs: [az1]\n" +
                     "    env:\n" +
                     "      persistent_disk_mount_options:\n" +
                     "      - noatime\n" +
                     "    instances: 1\n" +
                     "    vm_type: vmType\n" +
                     "    release: release\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: \"network\"\n" +
                     "    jobs:\n" +
                     "      - name: job\n" +
                     "        release: release\n" +
                     "      - name: complex-job\n" +
                     "        release: release\n" +
                     "        consumes:\n" +
                     "          consumes-a:\n" +
                     "            from: consumes-a\n" +
                     "          consumes-b:\n" +
                     "            from: consumes-b\n" +
                     "        provides:\n" +
                     "          provides-a:\n" +
                     "            as: provides-a\n" +
                     "          provides-b:\n" +
                     "            as: provides-b\n" +
                     "      - name: complex-job\n" +
                     "        release: release\n" +
                     "        consumes:\n" +
                     "          consumes-a:\n" +
                     "            from: consumes-a\n" +
                     "          consumes-b:\n" +
                     "            from: consumes-b\n" +
                     "        provides:\n" +
                     "          provides-a:\n" +
                     "            as: provides-a\n" +
                     "          provides-b:\n" +
                     "            as: provides-b\n" +
                     "        properties:\n" +
                     "          string: string\n" +
                     "          list:\n" +
                     "          - list1\n" +
                     "          - list2\n" +
                     "          - list3\n" +
                     "          map:\n" +
                     "            map1: value\n" +
                     "            map2:\n" +
                     "            - value1\n" +
                     "            - value2\n" +
                     "            map3:\n" +
                     "              map31: value\n" +
                     "              map32: value\n" +
                     "      - name: complex-job\n" +
                     "        release: release\n" +
                     "        consumes:\n" +
                     "          consumes-a:\n" +
                     "            from: consumes-a\n" +
                     "          consumes-b:\n" +
                     "            from: consumes-b\n" +
                     "        provides:\n" +
                     "          provides-a:\n" +
                     "            as: provides-a\n" +
                     "          provides-b:\n" +
                     "            as: provides-b\n" +
                     "        properties:\n" +
                     "          string: string\n" +
                     "          list:\n" +
                     "          - list1\n" +
                     "          - list2\n" +
                     "          - list3\n" +
                     "          map:\n" +
                     "            map1: value\n" +
                     "            map2:\n" +
                     "            - value1\n" +
                     "            - value2\n" +
                     "            map3:\n" +
                     "              map31: value\n" +
                     "              map32: value\n" +
                     "update:\n" +
                     "  canaries: 1\n" +
                     "  max_in_flight: 1\n" +
                     "  serial: false\n" +
                     "  canary_watch_time: 1000-60000\n" +
                     "  update_watch_time: 1000-60000\n" +
                     "variables:\n" +
                     "    - name: password\n" +
                     "      type: password\n" +
                     "      options:\n" +
                     "        length: 40\n" +
                     "    - name: ca\n" +
                     "      type: certificate\n" +
                     "      options:\n" +
                     "        common_name: ca\n" +
                     "        is_ca: true\n" +
                     "    - name: certificate\n" +
                     "      type: certificate\n" +
                     "      options:\n" +
                     "        common_name: certificate\n" +
                     "        ca: ca\n",


        ]
    }

    private String randomUUIDString() {
        randomUUID().toString()
    }


}
