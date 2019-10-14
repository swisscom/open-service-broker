package com.swisscom.cloud.sb.broker.services.bosh.client

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.http.trafficlistener.ConsoleNotifyingWiremockNetworkTrafficListener
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshCloudConfigRequest.configRequest
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDirectorTask.Event.State.UNKNOWN
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDirectorTask.State.PROCESSING
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshDirectorTask.State.QUEUED
import static com.swisscom.cloud.sb.broker.services.bosh.client.BoshWebClientTest.BoshInfoContentTransformer.of
import static org.apache.commons.lang3.StringUtils.isNumeric

class BoshWebClientTest extends Specification {

    private final static Logger LOGGER = LoggerFactory.getLogger(BoshWebClientTest.class)

    private final static boolean BOSH_MOCKED = Boolean.valueOf(System.getProperty("bosh.mocked"))
    private final static boolean BOSH_REPLIES_PERSISTED = Boolean.valueOf(System.getProperty("bosh.persisted"))
    private static final String BOSH_INFO_TRANSFORMER_NAME = "bosh-info"

    private final static String BOSH_BASE_URL = System.getProperty("bosh.url")
    private final static String BOSH_USERNAME = System.getProperty("bosh.username")
    private final static char[] BOSH_PASSWORD = System.getProperty("bosh.password").toCharArray()

    private static final String UAA_URL = System.getProperty("uaa.url")

    private static final String BOSH_TEMPLATE_TEST_NAME = "test-bosh-template-v1"

    @ClassRule
    public static WireMockRule boshWireMock

    @ClassRule
    public static WireMockRule uaaWireMock
    public static final UUID TEST_UUID = UUID.nameUUIDFromBytes(BoshWebClientTest.class.getName().getBytes("UTF-8"))

    def setupSpec() {
        WireMockConfiguration boshWireMockConfiguration = options().
                withRootDirectory("src/test/resources/bosh").
                port(35555).
                useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE).
                extensions(of(UAA_URL, "http://localhost:18443", BOSH_INFO_TRANSFORMER_NAME))


        WireMockConfiguration uaaWireMockConfiguration = options().
                withRootDirectory("src/test/resources/uaa").
                useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE).
                port(18443)

        if (LOGGER.isTraceEnabled()) {
            boshWireMockConfiguration = boshWireMockConfiguration.networkTrafficListener(
                    new ConsoleNotifyingWiremockNetworkTrafficListener())
            uaaWireMockConfiguration = uaaWireMockConfiguration.networkTrafficListener(
                    new ConsoleNotifyingWiremockNetworkTrafficListener())
        }

        boshWireMock = new WireMockRule(boshWireMockConfiguration)
        uaaWireMock = new WireMockRule(uaaWireMockConfiguration)
        boshWireMock.start()
        uaaWireMock.start()

        if (!BOSH_MOCKED) {
            LOGGER.
                    info("Start recording with bosh wiremock targeting '${BOSH_BASE_URL}' and uaa wiremock targeting '${UAA_URL}'")
            boshWireMock.startRecording(recordSpec().
                    forTarget(BOSH_BASE_URL).
                    extractBinaryBodiesOver(10240).
                    extractTextBodiesOver(256).
                    makeStubsPersistent(BOSH_REPLIES_PERSISTED).
                    transformers(BOSH_INFO_TRANSFORMER_NAME))

            uaaWireMock.startRecording(recordSpec().
                    forTarget(UAA_URL).
                    extractBinaryBodiesOver(10240).
                    extractTextBodiesOver(256).
                    makeStubsPersistent(BOSH_REPLIES_PERSISTED)
            )
        }
    }

    def cleanupSpec() {
        if (!BOSH_MOCKED) {
            boshWireMock.stopRecording()
            uaaWireMock.stopRecording()
        }
        boshWireMock.stop()
        uaaWireMock.stop()
    }

    @Shared
    BoshWebClient boshWebClient

    void setup() {
        boshWebClient = BoshWebClient.boshWebClient(boshWireMock.baseUrl(), BOSH_USERNAME, BOSH_PASSWORD)
    }

    void cleanup() {
    }

    def "should fetch /info from certain BOSH"() {
        when:
        BoshInfo boshInfo = boshWebClient.fetchBoshInfo()

        then:
        boshInfo != null
        !boshInfo.uuid.isEmpty()
        !boshInfo.name.isEmpty()
        boshInfo.userAuthentication != null
        LOGGER.info("/info {}", boshInfo)
    }

    def "should post to /configs"() {
        when:
        BoshCloudConfig response = boshWebClient.requestConfig(request)

        then:
        response != null
        !response.name.isEmpty()
        response.createdAt != null
        !response.id.isEmpty()
        !response.type.isEmpty()
        LOGGER.info("POST /configs {}", response)

        where:
        request = configRequest().
                name("test").
                type("cloud").
                content(getBoshTestConfig()).
                build()
    }

    def "should get all /configs"() {
        when:
        Collection<BoshCloudConfig> result = boshWebClient.getConfigs()

        then:
        result != null
        !result.isEmpty()
        for (BoshCloudConfig config : result) {
            LOGGER.info("configs/{} {}", config.id, config)
        }
    }


    def "should delete a /config"() {
        given: "An existing config"
        BoshCloudConfig response = boshWebClient.requestConfig(request)

        response != null
        !response.name.isEmpty()
        response.createdAt != null
        !response.id.isEmpty()
        !response.type.isEmpty()

        when:
        boshWebClient.deleteConfig(configRequest().id(response.getId()).build())

        then:
        noExceptionThrown()

        where:
        request = configRequest().
                name(TEST_UUID.toString()).
                type("cloud").
                content(getBoshTestConfig()).
                build()
    }


    def "should post to /deployments"() {
        given:
        BoshCloudConfig response = boshWebClient.requestConfig(request)
        response != null
        !response.name.isEmpty()
        response.createdAt != null
        !response.id.isEmpty()
        !response.type.isEmpty()
        LOGGER.info("POST /configs {}", response)

        when:
        BoshDeployment boshDeployment = boshWebClient.requestDeployment(request.getName(), ymlContent)

        then:
        boshDeployment != null
        !boshDeployment.taskId.isEmpty()
        isNumeric(boshDeployment.taskId)
        LOGGER.info("/deployments {}", boshDeployment)

        where:
        request = configRequest().
                name("test").
                type("cloud").
                content(getBoshTestConfig()).
                build()
        ymlContent = getBoshTestTemplate()
    }

    def "should delete /deployments"(){
        given: "A successful boshDeployment"
        BoshDeployment boshDeployment = boshWebClient.requestDeployment("test", ymlContent)

        when:
        BoshDeployment deleted = boshWebClient.deleteDeployment("test-bosh-template-v1")

        then:
        deleted != null
        !deleted.taskId.isEmpty()
        isNumeric(deleted.taskId)
        LOGGER.info("/deployments {}", deleted)

        where:
        request = configRequest().
                name("test").
                type("cloud").
                content(getBoshTestConfig()).
                build()
        ymlContent = getBoshTestTemplate()
    }

    def "should get certain /deployments"() {
        given: "A successful boshDeployment"
        BoshDeployment boshDeployment = boshWebClient.requestDeployment("test", ymlContent)

        and:
        boshDeployment != null
        !boshDeployment.taskId.isEmpty()
        isNumeric(boshDeployment.taskId)
        LOGGER.info("/deployments {}", boshDeployment)

        when:
        BoshDeployment result = boshWebClient.getDeployment(BOSH_TEMPLATE_TEST_NAME)

        then:
        result != null
        LOGGER.info("get /deployments/{} {}", BOSH_TEMPLATE_TEST_NAME, result)

        where:
        ymlContent = getBoshTestTemplateWithRandomName()
    }

    def "should get all /deployments"() {
        when:
        Collection<BoshDeployment> result = boshWebClient.getDeployments()

        then:
        result != null
        !result.isEmpty()
        for (BoshDeployment deployment : result) {
            LOGGER.info("get /deployments {}", deployment)
        }
    }

    def "should get all tasks associated with certain deployment"() {
        when:
        Collection<BoshDirectorTask> result = boshWebClient.getTasksAssociatedWithDeployment(BOSH_TEMPLATE_TEST_NAME)

        then:
        result != null
        LOGGER.info("################################ tasks associated to deployment '{}'", BOSH_TEMPLATE_TEST_NAME)
        for (BoshDirectorTask task : result) {
            assert !task.id.isEmpty()
            assert task.deployment == BOSH_TEMPLATE_TEST_NAME
            if (task.state != PROCESSING && task.state != QUEUED) {
                assert task.timestamp > 0
            }
            LOGGER.info("{}", task)
        }
        LOGGER.info("################################")
    }

    def "should get a /task"() {
        given: "A deployment posted which has a task associated"
        BoshDeployment boshDeployment = boshWebClient.requestDeployment("test", ymlContent)

        and:
        boshDeployment != null
        !boshDeployment.taskId.isEmpty()
        isNumeric(boshDeployment.taskId)
        LOGGER.info("/deployments {}", boshDeployment)

        when:
        BoshDirectorTask task = boshWebClient.getTask(boshDeployment.taskId)

        then:
        task != null
        task.id == boshDeployment.taskId
        !task.description.isEmpty()
        task.state != BoshDirectorTask.State.UNKNOWN
        if (task.state != PROCESSING && task.state != QUEUED) {
            task.timestamp > 0
        }
        !task.user.isEmpty()
        !task.deployment.isEmpty()
        task.events.isEmpty()
        LOGGER.info("tasks/{} {}", task.id, task)

        where:
        ymlContent = getBoshTestTemplateWithRandomName()
    }

    @Unroll
    def "should get a /task with its /events"() {
        when:
        BoshDirectorTask task = boshWebClient.getTaskWithEvents(taskId)

        then:
        task != null
        task.id == taskId
        !task.description.isEmpty()
        task.state != BoshDirectorTask.State.UNKNOWN
        task.timestamp > 0
        !task.user.isEmpty()
        task.deployment != null
        LOGGER.info("tasks/{} {}", task.id, task)
        for (BoshDirectorTask.Event event : task.events) {
            if (event.hasError()) {
                assert event.time > 0
                assert !event.error.message.isEmpty()
                assert event.error.code > 0
            } else {
                assert event.time > 0
                assert event.index > 0
                assert event.total > 0
                assert event.state != UNKNOWN
                assert !event.stage.isEmpty()
                assert !event.task.isEmpty()
            }
            LOGGER.info("tasks/{}/events {}", task.id, event)
        }

        where:
        taskId | _
        "1"    | _
        "21"   | _

    }


    def "should get existing /stemcells"() {
        when:
        Collection<BoshStemcell> result = boshWebClient.getStemcells()

        then:
        result != null
        !result.isEmpty()
        for (BoshStemcell stemCell : result) {
            LOGGER.info("get /stemcalls {}", stemCell)
        }
    }

    def "should get existing /releases"() {
        when:
        Collection<BoshRelease> result = boshWebClient.getReleases()

        then:
        result != null
        !result.isEmpty()
        for (BoshRelease release : result) {
            LOGGER.info("get /stemcells {}", release)
        }
    }


    static class BoshInfoContentTransformer extends ResponseTransformer {
        private static final Logger LOGGER = LoggerFactory.getLogger(BoshInfoContentTransformer.class);

        private final String originalUaaUrl;
        private final String replacementUaaUrl;
        private final String name;

        private BoshInfoContentTransformer(String originalUaaUrl, String replacementUaaUrl, String name) {
            this.originalUaaUrl = originalUaaUrl;
            this.replacementUaaUrl = replacementUaaUrl;
            this.name = name;
        }

        static BoshInfoContentTransformer of(String originalUaaUrl, String replacementUaaUrl, String name) {
            return new BoshInfoContentTransformer(originalUaaUrl, replacementUaaUrl, name);
        }

        @Override
        public Response transform(Request request,
                                  Response responseDefinition,
                                  FileSource files,
                                  Parameters parameters) {
            if (request.getAbsoluteUrl().endsWith("/info")) {
                LOGGER.debug("transforming for url: " + request.getAbsoluteUrl());
                String body = new String(responseDefinition.getBody());
                return Response.Builder.like(responseDefinition)
                               .but()
                               .body(body.replaceAll('"' + originalUaaUrl + '"',
                                                     '"' + replacementUaaUrl + '"'))
                               .build();
            }
            return responseDefinition;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private getBoshTestConfig() {
        return "networks:\n" +
               "  - name: \"77ee34cd-f079-4847-8461-cb1e76bb1705\"\n" +
               "    subnets:\n" +
               "      - azs: [\"z1\",\"z2\",\"z3\"]\n" +
               "        cloud_properties:\n" +
               "          name: \"mysql-test\"\n" +
               "        dns:\n" +
               "          - \"8.8.8.8\"\n" +
               "        gateway: \"10.0.0.1\"\n" +
               "        range: \"10.0.0.0/24\"\n" +
               "        reserved: [\"10.0.0.1-10.0.0.9\",\"10.0.0.15-10.0.0.255\"]\n" +
               "        static: []\n" +
               "    type: manual\n" +
               "vm_types:\n" +
               "  - cloud_properties:\n" +
               "      cpu: 1\n" +
               "      ram: 512\n" +
               "      disk: 1024\n" +
               "    name: \"77ee34cd-f079-4847-8461-cb1e76bb1705\"\n" +
               "disk_types:\n" +
               "  - disk_size: 1024\n" +
               "    name: \"77ee34cd-f079-4847-8461-cb1e76bb1705\""
    }

    private String getBoshTestTemplate() {
        return "name: \"test-bosh-template-v1\"\n" +
               "\n" +
               "releases:\n" +
               "  - name: pxc\n" +
               "    version: latest\n" +
               "\n" +
               "stemcells:\n" +
               "  - alias: ubuntu\n" +
               "    os: ubuntu-xenial\n" +
               "    version: latest\n" +
               "\n" +
               "instance_groups:\n" +
               "  - name: \"test_pxc\"\n" +
               "    azs: [z1]\n" +
               "    instances: 1\n" +
               "    vm_type: \"77ee34cd-f079-4847-8461-cb1e76bb1705\"\n" +
               "    stemcell: ubuntu\n" +
               "    networks:\n" +
               "      - name: \"77ee34cd-f079-4847-8461-cb1e76bb1705\"\n" +
               "    jobs:\n" +
               "      - name: pxc-mysql\n" +
               "        release: pxc\n" +
               "        properties:\n" +
               "          admin_password: ((cf_mysql_mysql_admin_password))\n" +
               "          tls:\n" +
               "            galera: ((galera_server_certificate))\n" +
               "            server: ((mysql_server_certificate))\n" +
               "\n" +
               "update:\n" +
               "  canaries: 1\n" +
               "  max_in_flight: 6\n" +
               "  serial: false\n" +
               "  canary_watch_time: 1000-60000\n" +
               "  update_watch_time: 1000-60000\n" +
               "\n" +
               "variables:\n" +
               "- name: cf_mysql_mysql_admin_password\n" +
               "  type: password\n" +
               "- name: pxc_galera_ca\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    is_ca: true\n" +
               "    common_name: pxc_galera_ca\n" +
               "- name: pxc_server_ca\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    is_ca: true\n" +
               "    common_name: pxc_server_ca\n" +
               "- name: galera_server_certificate\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    ca: pxc_galera_ca\n" +
               "    extended_key_usage: [ \"server_auth\", \"client_auth\" ]\n" +
               "    common_name: galera_server_certificate\n" +
               "- name: mysql_server_certificate\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    ca: pxc_server_ca\n" +
               "    common_name: mysql_server_certificate"
    }

    private String getBoshTestTemplateWithRandomName() {
        return "name: \"" + TEST_UUID + "\"\n" +
               "\n" +
               "releases:\n" +
               "  - name: pxc\n" +
               "    version: latest\n" +
               "\n" +
               "stemcells:\n" +
               "  - alias: ubuntu\n" +
               "    os: ubuntu-xenial\n" +
               "    version: latest\n" +
               "\n" +
               "instance_groups:\n" +
               "  - name: \"test_pxc\"\n" +
               "    azs: [z1]\n" +
               "    instances: 1\n" +
               "    vm_type: \"77ee34cd-f079-4847-8461-cb1e76bb1705\"\n" +
               "    stemcell: ubuntu\n" +
               "    networks:\n" +
               "      - name: \"77ee34cd-f079-4847-8461-cb1e76bb1705\"\n" +
               "    jobs:\n" +
               "      - name: pxc-mysql\n" +
               "        release: pxc\n" +
               "        properties:\n" +
               "          admin_password: ((cf_mysql_mysql_admin_password))\n" +
               "          tls:\n" +
               "            galera: ((galera_server_certificate))\n" +
               "            server: ((mysql_server_certificate))\n" +
               "\n" +
               "update:\n" +
               "  canaries: 1\n" +
               "  max_in_flight: 6\n" +
               "  serial: false\n" +
               "  canary_watch_time: 1000-60000\n" +
               "  update_watch_time: 1000-60000\n" +
               "\n" +
               "variables:\n" +
               "- name: cf_mysql_mysql_admin_password\n" +
               "  type: password\n" +
               "- name: pxc_galera_ca\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    is_ca: true\n" +
               "    common_name: pxc_galera_ca\n" +
               "- name: pxc_server_ca\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    is_ca: true\n" +
               "    common_name: pxc_server_ca\n" +
               "- name: galera_server_certificate\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    ca: pxc_galera_ca\n" +
               "    extended_key_usage: [ \"server_auth\", \"client_auth\" ]\n" +
               "    common_name: galera_server_certificate\n" +
               "- name: mysql_server_certificate\n" +
               "  type: certificate\n" +
               "  options:\n" +
               "    ca: pxc_server_ca\n" +
               "    common_name: mysql_server_certificate"
    }


}
