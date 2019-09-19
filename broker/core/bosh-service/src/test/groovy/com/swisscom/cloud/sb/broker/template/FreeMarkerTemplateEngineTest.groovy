package com.swisscom.cloud.sb.broker.template

import com.github.maltalex.ineter.base.IPAddress
import com.github.maltalex.ineter.range.IPv4Range
import com.github.maltalex.ineter.range.IPv4Subnet
import com.google.common.collect.ImmutableMap
import com.swisscom.cloud.sb.broker.services.bosh.client.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfig.Network.Type.MANUAL
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.InstanceGroup.Job.job
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.InstanceGroup.instanceGroup
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.Variable.Type.CERTIFICATE
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.Variable.Type.CERTIFICATE_CA
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.Variable.Type.PASSWORD
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.Variable.variable
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDeploymentRequest.deploymentRequest
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshRelease.release
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshStemcell.stemcell
import static java.util.Collections.singletonMap
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
    public static final BoshCloudConfig.AvailabilityZone Z_1 = ImmutableAvailabilityZone.
            builder().
            name("z1").
            cpi("z1").
            datacenter("WL01", "AC-OLT-Ext-01").
            build()
    public static final BoshCloudConfig.AvailabilityZone Z_2 = ImmutableAvailabilityZone.
            builder().
            name("z2").
            cpi("z2").
            datacenter("WL01", "AC-ZHH-Ext-01").
            build()
    public static final BoshCloudConfig.AvailabilityZone Z_3 = ImmutableAvailabilityZone.
            builder().
            name("z3").
            cpi("z3").
            datacenter("WL01", "AC-ZHH-Ext-02").
            build()
    public static final BoshCloudConfig.Network MYSQL_1_NODE_NETWORK = ImmutableNetwork.builder().
            name("mysql_nodes").
            type(MANUAL).
            addSubnet(ImmutableSubnet.builder().
                    name("subnet-z1").
                    addAvailabilityZone("z1").
                    cloudProperties(singletonMap("name",
                                                 "vxw-dvs-56-virtualwire-23-sid-29016-LS-AC4-PSC_PUB-WL01-UCS4-OLT")).
                    addDns(IPAddress.of("100.106.160.36")).
                    addDns(IPAddress.of("100.106.160.37")).
                    gateway(IPAddress.of("100.106.160.1")).
                    range(IPv4Subnet.of("100.106.160.0/19")).
                    addReserved(IPv4Range.of("100.106.160.2", "100.106.170.10")).
                    addReserved(IPv4Range.of("100.106.170.251", "100.106.191.254")).
                    build()).
            build()

    public static final BoshCloudConfig.Network MYSQL_NODES_NETWORK = ImmutableNetwork.builder().
            name("mysql_nodes").
            type(MANUAL).
            addSubnet(ImmutableSubnet.builder().
                    name("subnet-z1").
                    addAvailabilityZone("z1").
                    cloudProperties(singletonMap("name",
                                                 "vxw-dvs-56-virtualwire-23-sid-29016-LS-AC4-PSC_PUB-WL01-UCS4-OLT")).
                    addDns(IPAddress.of("100.106.160.36")).
                    addDns(IPAddress.of("100.106.160.37")).
                    gateway(IPAddress.of("100.106.160.1")).
                    range(IPv4Subnet.of("100.106.160.0/19")).
                    addReserved(IPv4Range.of("100.106.160.2", "100.106.170.10")).
                    addReserved(IPv4Range.of("100.106.170.251", "100.106.191.254")).
                    build()).
            addSubnet(ImmutableSubnet.builder().
                    name("subnet-z2").
                    addAvailabilityZone("z2").
                    cloudProperties(singletonMap("name",
                                                 "vxw-dvs-78-virtualwire-19-sid-30016-LS-AC5-PSC_PUB-WL01-UCS4-ZHH")).
                    addDns(IPAddress.of("100.106.192.36")).
                    addDns(IPAddress.of("100.106.192.37")).
                    gateway(IPAddress.of("100.106.192.1")).
                    range(IPv4Subnet.of("100.106.192.0/19")).
                    addReserved(IPv4Range.of("100.106.192.2", "100.106.202.10")).
                    addReserved(IPv4Range.of("100.106.202.251", "100.106.223.254")).
                    build()).
            addSubnet(ImmutableSubnet.builder().
                    name("subnet-z3").
                    addAvailabilityZone("z3").
                    cloudProperties(singletonMap("name",
                                                 "vxw-dvs-73-virtualwire-17-sid-31016-LS-AC6-PSC_PUB-WL01-UCS1-ZHH")).
                    addDns(IPAddress.of("100.106.224.36")).
                    addDns(IPAddress.of("100.106.224.37")).
                    gateway(IPAddress.of("100.106.224.1")).
                    range(IPv4Subnet.of("100.106.224.0/19")).
                    addReserved(IPv4Range.of("100.106.224.2", "100.106.234.10")).
                    addReserved(IPv4Range.of("100.106.234.251", "100.106.255.254")).
                    build()).
            build()
    public static final String PASSWORD_BOSH = '$6$elefant$Y/jtYmxqqxfU4zEPdXtlM./v/dP7F7l1b8CrP5CvKjg8vonJLlXV0UuKkQBUE/JEgPHK2loUozMivZmgp1GI4/'
    public static final String BOSH_CLOUD_CONFIG_TEMPLATE_ID = "bosh-cloud-config.ftlh"

    @Shared
    TemplateEngine templateEngine

    @Shared
    BoshDeploymentRequest.InstanceGroup.Builder simpleInstanceGroup = instanceGroup().
            name("instanceGroup").
            numberOfInstances(1).
            putEnvironmentProperty("persistent_disk_mount_options", ["noatime"]).
            addAvailabilityZone("az1").
            vmType("vmType").
            persistentDiskType("persistentDiskType").
            addNetwork("network").
            addJob(job().name("job").release(release().name("release").build()).build())

    def setupSpec() {
        templateEngine = FreeMarkerTemplateEngine.newInstance()
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
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.stemcell(SIMPLE_STEMCELL).build(),
                simpleInstanceGroup.
                        stemcell(SIMPLE_STEMCELL).
                        addJob("complex-job",
                               SIMPLE_RELEASE,
                               ["consumes-a", "consumes-b"],
                               ["provides-a", "provides-b"],
                               Collections.emptyMap()).
                        build(),
                simpleInstanceGroup.
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

                simpleInstanceGroup.
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
                     "    persistent_disk_type: persistentDiskType\n" +
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
                     "    persistent_disk_type: persistentDiskType\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: network\n" +
                     "        default:\n" +
                     "        - dns\n" +
                     "        - gateway\n" +
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
                     "    persistent_disk_type: persistentDiskType\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: network\n" +
                     "        default:\n" +
                     "        - dns\n" +
                     "        - gateway\n" +
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
                     "    persistent_disk_type: persistentDiskType\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: network\n" +
                     "        default:\n" +
                     "        - dns\n" +
                     "        - gateway\n" +
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
                     "    persistent_disk_type: persistentDiskType\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: network\n" +
                     "        default:\n" +
                     "        - dns\n" +
                     "        - gateway\n" +
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
                     "    persistent_disk_type: persistentDiskType\n" +
                     "    stemcell: stemcell\n" +
                     "    networks:\n" +
                     "      - name: network\n" +
                     "        default:\n" +
                     "        - dns\n" +
                     "        - gateway\n" +
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

    @Unroll
    def "process BOSH cloud config template with #request"() {
        given:
        Map<String, Object> modelMap = ImmutableMap.builder().
                put("cloudConfig", request).
                build()

        when:
        String result = templateEngine.process(BOSH_CLOUD_CONFIG_TEMPLATE_ID, modelMap)

        then:
        result != null
        result == expected
        LOGGER.debug("Result:\n" + result)

        where:
        request << [
                BoshCloudConfig.cloudConfig().name(randomUUIDString()).
                        addAvailabilityZone(Z_1).
                        addAvailabilityZone(Z_2).
                        addAvailabilityZone(Z_3).
                        compilation(ImmutableCompilation.builder().
                                availabilityZone(Z_1).
                                network(MYSQL_NODES_NETWORK).
                                environment(ImmutableMap.of("bosh", singletonMap("password", PASSWORD_BOSH))).
                                build()).
                        build(),

                BoshCloudConfig.cloudConfig().name(randomUUIDString()).
                        compilation(ImmutableCompilation.builder().
                                availabilityZone(Z_1).
                                network(MYSQL_NODES_NETWORK).
                                environment(ImmutableMap.of("bosh", singletonMap("password", PASSWORD_BOSH))).
                                build()).
                        addNetwork(MYSQL_1_NODE_NETWORK)
                               .build(),

                BoshCloudConfig.cloudConfig().name(randomUUIDString()).
                        addAvailabilityZone(Z_1).
                        addAvailabilityZone(Z_2).
                        addAvailabilityZone(Z_3).
                        compilation(ImmutableCompilation.builder().
                                availabilityZone(Z_1).
                                network(MYSQL_NODES_NETWORK).
                                environment(ImmutableMap.of("bosh", singletonMap("password", PASSWORD_BOSH))).
                                build()).
                        addNetwork(MYSQL_NODES_NETWORK).
                        addDiskType("mysql.xsmall", 38400).
                        addDiskType("mysql.small", 76800).
                        addDiskType("mysql.medium", 153600).
                        addDiskType("mysql.large", 307200).
                        addDiskType("mysql.xlarge", 614400).

                        addVmType("default", 2, 4096, 20480, "apc.c2m4096r20").
                        addVmType("compilation", 4, 4096, 51200, "apc.c4m4096r50").

                        addVmType("small_errand", 1, 2048, 10240, "apc.c1m2048r10").
                        addVmType("errand", 2, 4096, 20480, "apc.c2m4096r20").
                        addVmType("large_errand", 4, 4096, 51200, "apc.c4m4096r50").

                        addVmType("small", 1, 2048, 10240, "apc.c1m2048r10").
                        addVmType("medium", 2, 4096, 20480, "apc.c2m4096r20").
                        addVmType("large", 4, 8192, 20480, "apc.c4m8192r20").

                        addVmType("bosh", 4, 8192, 51200, "apc.c4m8192r50").

                        addVmType("mysql.xsmall_mysql", 1, 2048, 20240, "apc.c1m2048r10").
                        addVmType("mysql.xsmall_proxy", 1, 1024, 10240, "apc.c1m1024r10").

                        addVmType("mysql.small_mysql", 2, 4096, 20240, "apc.c2m4096r10").
                        addVmType("mysql.small_proxy", 1, 1024, 10240, "apc.c1m1024r10").

                        addVmType("mysql.medium_mysql", 4, 8192, 20240, "apc.c4m8192r10").
                        addVmType("mysql.medium_proxy", 1, 1024, 10240, "apc.c1m1024r10").

                        addVmType("mysql.large_mysql", 8, 16384, 40240, "apc.c8m16384r10").
                        addVmType("mysql.large_proxy", 1, 1024, 10240, "apc.c1m1024r10").

                        addVmType("mysql.xlarge_mysql", 16, 32768, 40240, "apc.c16m32768r10").
                        addVmType("mysql.xlarge_proxy", 1, 1024, 10240, "apc.c1m1024r10").

                        build()
        ]

        expected << [
                "azs:\n" +
                "- name: z1\n" +
                "  cpi: z1\n" +
                "  cloud_properties:\n" +
                "    datacenters:\n" +
                "    - name: WL01\n" +
                "      clusters:\n" +
                "      - AC-OLT-Ext-01: {}\n" +
                "- name: z2\n" +
                "  cpi: z2\n" +
                "  cloud_properties:\n" +
                "    datacenters:\n" +
                "    - name: WL01\n" +
                "      clusters:\n" +
                "      - AC-ZHH-Ext-01: {}\n" +
                "- name: z3\n" +
                "  cpi: z3\n" +
                "  cloud_properties:\n" +
                "    datacenters:\n" +
                "    - name: WL01\n" +
                "      clusters:\n" +
                "      - AC-ZHH-Ext-02: {}\n" +
                "compilation:\n" +
                "  vm_type: compilation\n" +
                "  network: mysql_nodes\n" +
                "  az: z1\n" +
                "  reuse_compilation_vms: true\n" +
                "  workers: 5\n" +
                "  env:\n" +
                "    bosh:\n" +
                "      password: \$6\$elefant\$Y/jtYmxqqxfU4zEPdXtlM./v/dP7F7l1b8CrP5CvKjg8vonJLlXV0UuKkQBUE/JEgPHK2loUozMivZmgp1GI4/\n"

                , "compilation:\n" +
                  "  vm_type: compilation\n" +
                  "  network: mysql_nodes\n" +
                  "  az: z1\n" +
                  "  reuse_compilation_vms: true\n" +
                  "  workers: 5\n" +
                  "  env:\n" +
                  "    bosh:\n" +
                  "      password: \$6\$elefant\$Y/jtYmxqqxfU4zEPdXtlM./v/dP7F7l1b8CrP5CvKjg8vonJLlXV0UuKkQBUE/JEgPHK2loUozMivZmgp1GI4/\n" +
                  "networks:\n" +
                  "- name: mysql_nodes\n" +
                  "  type: manual\n" +
                  "  subnets:\n" +
                  "  - name: subnet-z1\n" +
                  "    az: z1\n" +
                  "    cloud_properties:\n" +
                  "      name: vxw-dvs-56-virtualwire-23-sid-29016-LS-AC4-PSC_PUB-WL01-UCS4-OLT\n" +
                  "    dns:\n" +
                  "    - 100.106.160.36\n" +
                  "    - 100.106.160.37\n" +
                  "    gateway: 100.106.160.1\n" +
                  "    range: 100.106.160.0/19\n" +
                  "    reserved:\n" +
                  "    - 100.106.160.2 - 100.106.170.10\n" +
                  "    - 100.106.170.251 - 100.106.191.254\n"

                , "azs:\n" +
                  "- name: z1\n" +
                  "  cpi: z1\n" +
                  "  cloud_properties:\n" +
                  "    datacenters:\n" +
                  "    - name: WL01\n" +
                  "      clusters:\n" +
                  "      - AC-OLT-Ext-01: {}\n" +
                  "- name: z2\n" +
                  "  cpi: z2\n" +
                  "  cloud_properties:\n" +
                  "    datacenters:\n" +
                  "    - name: WL01\n" +
                  "      clusters:\n" +
                  "      - AC-ZHH-Ext-01: {}\n" +
                  "- name: z3\n" +
                  "  cpi: z3\n" +
                  "  cloud_properties:\n" +
                  "    datacenters:\n" +
                  "    - name: WL01\n" +
                  "      clusters:\n" +
                  "      - AC-ZHH-Ext-02: {}\n" +
                  "compilation:\n" +
                  "  vm_type: compilation\n" +
                  "  network: mysql_nodes\n" +
                  "  az: z1\n" +
                  "  reuse_compilation_vms: true\n" +
                  "  workers: 5\n" +
                  "  env:\n" +
                  "    bosh:\n" +
                  "      password: \$6\$elefant\$Y/jtYmxqqxfU4zEPdXtlM./v/dP7F7l1b8CrP5CvKjg8vonJLlXV0UuKkQBUE/JEgPHK2loUozMivZmgp1GI4/\n" +
                  "networks:\n" +
                  "- name: mysql_nodes\n" +
                  "  type: manual\n" +
                  "  subnets:\n" +
                  "  - name: subnet-z1\n" +
                  "    az: z1\n" +
                  "    cloud_properties:\n" +
                  "      name: vxw-dvs-56-virtualwire-23-sid-29016-LS-AC4-PSC_PUB-WL01-UCS4-OLT\n" +
                  "    dns:\n" +
                  "    - 100.106.160.36\n" +
                  "    - 100.106.160.37\n" +
                  "    gateway: 100.106.160.1\n" +
                  "    range: 100.106.160.0/19\n" +
                  "    reserved:\n" +
                  "    - 100.106.160.2 - 100.106.170.10\n" +
                  "    - 100.106.170.251 - 100.106.191.254\n" +
                  "  - name: subnet-z2\n" +
                  "    az: z2\n" +
                  "    cloud_properties:\n" +
                  "      name: vxw-dvs-78-virtualwire-19-sid-30016-LS-AC5-PSC_PUB-WL01-UCS4-ZHH\n" +
                  "    dns:\n" +
                  "    - 100.106.192.36\n" +
                  "    - 100.106.192.37\n" +
                  "    gateway: 100.106.192.1\n" +
                  "    range: 100.106.192.0/19\n" +
                  "    reserved:\n" +
                  "    - 100.106.192.2 - 100.106.202.10\n" +
                  "    - 100.106.202.251 - 100.106.223.254\n" +
                  "  - name: subnet-z3\n" +
                  "    az: z3\n" +
                  "    cloud_properties:\n" +
                  "      name: vxw-dvs-73-virtualwire-17-sid-31016-LS-AC6-PSC_PUB-WL01-UCS1-ZHH\n" +
                  "    dns:\n" +
                  "    - 100.106.224.36\n" +
                  "    - 100.106.224.37\n" +
                  "    gateway: 100.106.224.1\n" +
                  "    range: 100.106.224.0/19\n" +
                  "    reserved:\n" +
                  "    - 100.106.224.2 - 100.106.234.10\n" +
                  "    - 100.106.234.251 - 100.106.255.254\n" +
                  "disk_types:\n" +
                  "- name: mysql.xsmall\n" +
                  "  disk_size: 38400\n" +
                  "- name: mysql.small\n" +
                  "  disk_size: 76800\n" +
                  "- name: mysql.medium\n" +
                  "  disk_size: 153600\n" +
                  "- name: mysql.large\n" +
                  "  disk_size: 307200\n" +
                  "- name: mysql.xlarge\n" +
                  "  disk_size: 614400\n" +
                  "vm_types:\n" +
                  "- name: default\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c2m4096r20\n" +
                  "    cpu: 2\n" +
                  "    ram: 4096\n" +
                  "    disk: 20480\n" +
                  "- name: compilation\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c4m4096r50\n" +
                  "    cpu: 4\n" +
                  "    ram: 4096\n" +
                  "    disk: 51200\n" +
                  "- name: small_errand\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m2048r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 2048\n" +
                  "    disk: 10240\n" +
                  "- name: errand\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c2m4096r20\n" +
                  "    cpu: 2\n" +
                  "    ram: 4096\n" +
                  "    disk: 20480\n" +
                  "- name: large_errand\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c4m4096r50\n" +
                  "    cpu: 4\n" +
                  "    ram: 4096\n" +
                  "    disk: 51200\n" +
                  "- name: small\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m2048r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 2048\n" +
                  "    disk: 10240\n" +
                  "- name: medium\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c2m4096r20\n" +
                  "    cpu: 2\n" +
                  "    ram: 4096\n" +
                  "    disk: 20480\n" +
                  "- name: large\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c4m8192r20\n" +
                  "    cpu: 4\n" +
                  "    ram: 8192\n" +
                  "    disk: 20480\n" +
                  "- name: bosh\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c4m8192r50\n" +
                  "    cpu: 4\n" +
                  "    ram: 8192\n" +
                  "    disk: 51200\n" +
                  "- name: mysql.xsmall_mysql\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m2048r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 2048\n" +
                  "    disk: 20240\n" +
                  "- name: mysql.xsmall_proxy\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m1024r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 1024\n" +
                  "    disk: 10240\n" +
                  "- name: mysql.small_mysql\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c2m4096r10\n" +
                  "    cpu: 2\n" +
                  "    ram: 4096\n" +
                  "    disk: 20240\n" +
                  "- name: mysql.small_proxy\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m1024r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 1024\n" +
                  "    disk: 10240\n" +
                  "- name: mysql.medium_mysql\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c4m8192r10\n" +
                  "    cpu: 4\n" +
                  "    ram: 8192\n" +
                  "    disk: 20240\n" +
                  "- name: mysql.medium_proxy\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m1024r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 1024\n" +
                  "    disk: 10240\n" +
                  "- name: mysql.large_mysql\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c8m16384r10\n" +
                  "    cpu: 8\n" +
                  "    ram: 16384\n" +
                  "    disk: 40240\n" +
                  "- name: mysql.large_proxy\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m1024r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 1024\n" +
                  "    disk: 10240\n" +
                  "- name: mysql.xlarge_mysql\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c16m32768r10\n" +
                  "    cpu: 16\n" +
                  "    ram: 32768\n" +
                  "    disk: 40240\n" +
                  "- name: mysql.xlarge_proxy\n" +
                  "  cloud_properties:\n" +
                  "    instance_type: apc.c1m1024r10\n" +
                  "    cpu: 1\n" +
                  "    ram: 1024\n" +
                  "    disk: 10240\n"
        ]
    }

    private String randomUUIDString() {
        randomUUID().toString()
    }


}
