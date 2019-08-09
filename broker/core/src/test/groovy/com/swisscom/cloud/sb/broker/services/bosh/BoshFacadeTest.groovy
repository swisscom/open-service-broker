package com.swisscom.cloud.sb.broker.services.bosh

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.common.base.Optional
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cloud.sb.broker.services.bosh.resources.BoshConfigResponse
import com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig
import com.swisscom.cloud.sb.broker.services.common.ServiceTemplate
import com.swisscom.cloud.sb.broker.services.common.TemplateConfig
import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.client.HttpServerErrorException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

import java.time.LocalDateTime

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static com.google.common.base.Strings.isNullOrEmpty
import static com.swisscom.cloud.sb.broker.services.bosh.BoshServiceDetailKey.*
import static com.swisscom.cloud.sb.broker.services.bosh.resources.GenericConfig.genericConfig
import static java.util.Collections.singletonList

/**
 * For launching this test recording mappings, you must have a <a href="https://bosh.io/docs/">bosh</a> with the
 * <a href="https://github.com/pivotal-cf-experimental/dummy-boshrelease">dummy-boshrelease</a> uploaded.
 */
// TODO: Test failing authentication
// TODO: Test basic authentication (without uaa)
@Stepwise
class BoshFacadeTest extends Specification {
    private static final Logger LOG = LoggerFactory.getLogger(BoshFacadeTest.class)

    private static final boolean BOSH_MOCKED = Boolean.valueOf(System.getProperty("bosh.mocked"))
    private static final String UAA_URL = System.getProperty("uaa.url")
    private static final String BOSH_URL = System.getProperty("bosh.url")
    private static final String BOSH_USERNAME = System.getProperty("bosh.username")
    private static final String BOSH_PASSWORD = System.getProperty("bosh.password")

    private static final String BOSH_INFO_TRANSFORMER_NAME = "bosh-info"
    private final String SERVICE_INSTANCE_GUID = '7eff4b56-be53-4925-a4fe-afde1e00111a'

    private final String DEPLOYMENT_ID = "d-${SERVICE_INSTANCE_GUID}"
    private static final String CLOUD_CONFIG_NAME = "bosh-dummy-cloud-config-template"
    private static final String CLOUD_CONFIG_PATH = "/bosh/" + CLOUD_CONFIG_NAME + ".yml"
    private static final String DEPLOYMENT_TEMPLATE_NAME = "bosh-dummy-template"
    private static final String DEPLOYMENT_TEMPLATE_PATH = "/bosh/" + DEPLOYMENT_TEMPLATE_NAME + ".yml"
    private static final Set<Parameter> EMPTY_PARAMETERS = Collections.emptySet()

    @Shared
    private String deployTaskId

    @Shared
    private String deleteDeploymentTaskId

    @Shared
    private BoshTemplateCustomizer templateCustomizer

    BoshBasedServiceConfig boshFacadeConfiguration
    BoshFacade sut

    @ClassRule
    public static WireMockRule boshWireMock = new WireMockRule(options().
            withRootDirectory("src/test/resources/boshFacade/bosh").
            port(35555).
            extensions(BoshInfoContentTransformer.of(UAA_URL,
                                                     "http://localhost:18443",
                                                     BOSH_INFO_TRANSFORMER_NAME)))

    @ClassRule
    public static WireMockRule uaaWireMock = new WireMockRule(options().
            withRootDirectory("src/test/resources/boshFacade/uaa").
            port(18443))

    def setupSpec() {
        templateCustomizer = Mock(BoshTemplateCustomizer)
        boshWireMock.start()
        uaaWireMock.start()
        if (!BOSH_MOCKED) {
            LOG.info("Start recording with bosh wiremock targeting '${BOSH_URL}' and uaa wiremock targeting '${UAA_URL}'")
            boshWireMock.startRecording(recordSpec().
                    forTarget(BOSH_URL).
                    extractBinaryBodiesOver(10240).
                    extractTextBodiesOver(256).
                    makeStubsPersistent(true).
                    captureHeader("Authorization").
                    transformers(BOSH_INFO_TRANSFORMER_NAME))

            uaaWireMock.startRecording(recordSpec().
                    forTarget(UAA_URL).
                    extractBinaryBodiesOver(10240).
                    extractTextBodiesOver(256).
                    captureHeader("Authorization").
                    makeStubsPersistent(true)
            )
        }
    }

    def setup() {
        this.boshFacadeConfiguration = createBoshFacadeConfig()
        sut = BoshFacade.of(this.boshFacadeConfiguration)

        LOG.info("Testing against {} and with URL '{}' with username '{}' and password '{}'",
                 BOSH_MOCKED ? "mocked bosh" : "live bosh",
                 boshFacadeConfiguration.getBoshDirectorBaseUrl(),
                 boshFacadeConfiguration.getBoshDirectorUsername(),
                 isNullOrEmpty(boshFacadeConfiguration.getBoshDirectorPassword()) ? " NO PASSWORD PROVIDED" :
                 "<CONFIDENTIAL>")
    }

    private BoshBasedServiceConfig createBoshFacadeConfig() {
        return new BoshBasedServiceConfig() {
            @Override
            String getPortRange() {
                return ""
            }

            @Override
            String getBoshManifestFolder() {
                return "."
            }

            @Override
            boolean getShuffleAzs() {
                return false
            }

            @Override
            List<GenericConfig> getGenericConfigs() {
                return singletonList(
                        genericConfig().
                                templateName(CLOUD_CONFIG_NAME).
                                type("cloud").
                                build())
            }

            @Override
            TemplateConfig getTemplateConfig() {
                return TemplateConfig.of([
                        new ServiceTemplate() {
                            String getName() {
                                CLOUD_CONFIG_NAME
                            }

                            String getVersion() {
                                "1.0.0"
                            }

                            List<String> getTemplates() {
                                [new File(this.getClass().getResource(CLOUD_CONFIG_PATH).file).text]
                            }
                        },
                        new ServiceTemplate() {
                            String getName() {
                                DEPLOYMENT_TEMPLATE_NAME
                            }

                            String getVersion() {
                                "1.0.0"
                            }

                            List<String> getTemplates() {
                                [new File(this.getClass().getResource(DEPLOYMENT_TEMPLATE_PATH).file).text]
                            }
                        }])
            }

            @Override
            List<String> getIpRanges() {
                return []
            }

            @Override
            List<String> getProtocols() {
                return []
            }

            @Override
            String getBoshDirectorBaseUrl() {
                return "http://localhost:35555"
            }

            @Override
            String getBoshDirectorUsername() {
                return BOSH_USERNAME
            }

            @Override
            String getBoshDirectorPassword() {
                return BOSH_PASSWORD
            }
        }
    }

    private static List<GenericConfig> genericConfigs() {
        [new GenericConfig() {
            @Override
            String getTemplateName() {
                return "test"
            }

            @Override
            String getType() {
                return "cloud"
            }
        }]
    }

    private BoshBasedServiceConfig createBoshBasedConfig(List<Object> params) {
        LOG.info(params[0].toString())
        new BoshBasedServiceConfig() {

            @Override
            String getPortRange() {
                LOG.info("getPortRange:")
                LOG.info(params[0].toString())
                return params[0]
            }

            @Override
            String getBoshManifestFolder() {
                return params[1]
            }

            @Override
            boolean getShuffleAzs() {
                return false
            }

            @Override
            List<GenericConfig> getGenericConfigs() {
                return params[2]
            }

            @Override
            TemplateConfig getTemplateConfig() {
                return params[3]
            }

            @Override
            List<String> getIpRanges() {
                return params[4]
            }

            @Override
            List<String> getProtocols() {
                return params[5]
            }

            @Override
            String getBoshDirectorBaseUrl() {
                return params[6]
            }

            @Override
            String getBoshDirectorUsername() {
                return params[7]
            }

            @Override
            String getBoshDirectorPassword() {
                return params[8]
            }
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

    @Unroll
    def "instantiation should fail if the passed configuration is wrong: #message"() {
        given:
        BoshBasedServiceConfig serviceConfig = createBoshBasedConfig([portRange, manifestFolder, genericConfigs, templateConfigs, ipRanges, protocols, baseUrl, user, pass])

        when:
        sut = BoshFacade.of(serviceConfig)

        then:
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        portRange | manifestFolder | genericConfigs   | templateConfigs | ipRanges | protocols | baseUrl | user | pass | message
        null      | "."            | genericConfigs() | []              | []       | []        | "a"     | "a"  | "a"  | "Port range cannot be null!"
        ""        | null           | genericConfigs() | []              | []       | []        | "a"     | "a"  | "a"  | "Bosh manifest folder cannot be null!"
        ""        | "."            | null             | []              | []       | []        | "a"     | "a"  | "a"  | "Bosh generic configs cannot be null!"
        ""        | "."            | genericConfigs() | null            | []       | []        | "a"     | "a"  | "a"  | "TemplateConfig cannot be null!"
        ""        | "."            | genericConfigs() | []              | null     | []        | "a"     | "a"  | "a"  | "IP ranges cannot be null!"
        ""        | "."            | genericConfigs() | []              | []       | null      | "a"     | "a"  | "a"  | "Protocols cannot be null!"
        ""        | "."            | genericConfigs() | []              | []       | []        | null    | "a"  | "a"  | "Bosh director base url cannot be empty!"
        ""        | "."            | genericConfigs() | []              | []       | []        | ""      | "a"  | "a"  | "Bosh director base url cannot be empty!"
        ""        | "."            | genericConfigs() | []              | []       | []        | "a"     | null | "a"  | "Bosh director username cannot be empty!"
        ""        | "."            | genericConfigs() | []              | []       | []        | "a"     | ""   | "a"  | "Bosh director username cannot be empty!"
        ""        | "."            | genericConfigs() | []              | []       | []        | "a"     | "a"  | null | "Bosh director password cannot be empty!"
        ""        | "."            | genericConfigs() | []              | []       | []        | "a"     | "a"  | ""   | "Bosh director password cannot be empty!"
        ""        | ""             | genericConfigs() | []              | []       | []        | "a"     | "a"  | "a"  | "Bosh manifest folder must be set when service templates is empty!"
    }

    def "should create generic config"() {
        when:
        List<BoshConfigResponse> boshConfigResponses = sut.handleTemplatingAndCreateConfigs(SERVICE_INSTANCE_GUID,
                                                                                            templateCustomizer)

        then:
        boshConfigResponses.size() == boshFacadeConfiguration.getGenericConfigs().size()
        BoshConfigResponse boshConfigResponse = boshConfigResponses.first()
        boshConfigResponse.getId() > 0
        boshConfigResponse.getType() == "cloud"
        boshConfigResponse.getCurrent()
        boshConfigResponse.getCreatedAt().isBefore(LocalDateTime.now())
        boshConfigResponse.getContent() == new File(this.getClass().
                getResource("/bosh/bosh-dummy-cloud-config-manifest.yml").file).text
        noExceptionThrown()
    }


    def "should work with optional configuration missing"() {
    }

    def "should create bosh deployment"() {
        when:
        Collection<ServiceDetail> details = sut.handleTemplatingAndCreateDeployment(SERVICE_INSTANCE_GUID,
                                                                                    DEPLOYMENT_TEMPLATE_NAME,
                                                                                    EMPTY_PARAMETERS,
                                                                                    templateCustomizer)
        deployTaskId = details.find {it.key == BOSH_TASK_ID_FOR_DEPLOY.key}.value

        then:
        details.find {it.key == BOSH_TASK_ID_FOR_DEPLOY.key}.value =~ /\/[0-9]+/
        details.find {it.key == BOSH_DEPLOYMENT_ID.key}.value == DEPLOYMENT_ID
        noExceptionThrown()
    }

    def "should return false to indicate that deployment creation is in progress"() {
        given: "LastOperationJobContext with correct deploy task id"
        List<ServiceDetail> details = new ArrayList<>([ServiceDetail.from(BOSH_TASK_ID_FOR_DEPLOY.key, deployTaskId)])
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)

        when:
        boolean boshTaskSuccessful = sut.isBoshDeployTaskSuccessful(context.serviceInstance.details)

        then:
        !boshTaskSuccessful
        noExceptionThrown()
    }

    def "should return true to indicate that deployment creation was successful"() {
        given: "Wait for deployment to run through"
        if (!BOSH_MOCKED) {
            sleep(180000)
        }
        List<ServiceDetail> details = singletonList(ServiceDetail.from(BOSH_TASK_ID_FOR_DEPLOY.key, deployTaskId))
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)

        when:
        boolean boshTaskSuccessful = sut.isBoshDeployTaskSuccessful(context.serviceInstance.details)

        then:
        boshTaskSuccessful
        noExceptionThrown()
    }

    def "should delete bosh deployment"() {
        given: "LastoperationJobContext with correct deployment id"
        List<ServiceDetail> details = singletonList(ServiceDetail.from(BOSH_DEPLOYMENT_ID.key, DEPLOYMENT_ID))
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)

        when:
        Optional<String> deploymentDeleteTaskId = sut.deleteBoshDeploymentIfExists(context)
        deleteDeploymentTaskId = deploymentDeleteTaskId.get()

        then:
        deploymentDeleteTaskId.get() =~ /\/[0-9]+/
        noExceptionThrown()
    }

    def "should return false to indicate that deployment deletion is in progress"() {
        given:
        List<ServiceDetail> details = singletonList(ServiceDetail.from(BOSH_TASK_ID_FOR_UNDEPLOY.key,
                                                                       deleteDeploymentTaskId))
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)

        when:
        boolean boshTaskSuccessful = sut.isBoshUndeployTaskSuccessful(context.serviceInstance.details)

        then:
        !boshTaskSuccessful
    }

    @Unroll
    def "should return true to indicate that deployment deletion was successful: #reason"() {
        given:
        if (!BOSH_MOCKED) {
            sleep(30000)
        }
        List<ServiceDetail> details = singletonList(ServiceDetail.from(serviceKey, taskId))
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)


        when:
        boolean boshTaskSuccessful = sut.isBoshUndeployTaskSuccessful(context.serviceInstance.details)

        then:
        boshTaskSuccessful
        noExceptionThrown()

        where:
        taskId                 | serviceKey                    | reason
        deleteDeploymentTaskId | BOSH_TASK_ID_FOR_UNDEPLOY.key | "Deployment successfully deleted by given task"
        null                   | "WRONG_KEY"                   | "Deletion key was not found"
    }

    @Unroll
    def "should fail getting task state of deployment deletion: #reason"() {
        given:
        if (!BOSH_MOCKED) {
            sleep(30000)
        }
        List<ServiceDetail> details = singletonList(ServiceDetail.from(serviceKey, taskId))
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)

        when:
        boolean boshTaskSuccessful = sut.isBoshUndeployTaskSuccessful(context.serviceInstance.details)

        then:
        !boshTaskSuccessful
        thrown(expectedException)

        where:
        taskId           | serviceKey                    | reason                        || expectedException
        "DOES_NOT_EXIST" | BOSH_TASK_ID_FOR_UNDEPLOY.key | "Deletion task was not found" || HttpServerErrorException
        ""               | BOSH_TASK_ID_FOR_UNDEPLOY.key | "Deletion task was not found" || IllegalArgumentException
        null             | BOSH_TASK_ID_FOR_UNDEPLOY.key | "Deletion taskId is null"     || NullPointerException
    }

    def "should delete generic config"() {
        when:
        sut.deleteConfig(SERVICE_INSTANCE_GUID, "cloud")

        then:
        // Second deletion will throw BoshResourceNotFoundException when this command is successful, see test below
        noExceptionThrown()
    }

    def "should throw BoshResourceNotFoundException when deleting non-existing generic config"() {
        when:
        sut.deleteConfig(SERVICE_INSTANCE_GUID, "cloud")

        then:
        thrown(BoshResourceNotFoundException)
    }

    def "should return Optional.absent when deleting non-existing deployment"() {
        given: "LastoperationJobContext with correct deployment id"
        List<ServiceDetail> details = singletonList(ServiceDetail.from(BOSH_DEPLOYMENT_ID.key, DEPLOYMENT_ID))
        ServiceInstance serviceInstance = new ServiceInstance(details: details)
        LastOperationJobContext context = new LastOperationJobContext(serviceInstance: serviceInstance)

        when:
        Optional<String> deploymentDeleteTaskId = sut.deleteBoshDeploymentIfExists(context.serviceInstance.details,
                                                                                   context.serviceInstance.guid)

        then:
        !deploymentDeleteTaskId.isPresent()
        noExceptionThrown()
    }

    @Unroll
    def "should fail creating config because #message"() {
        when:
        List<BoshConfigResponse> boshConfigResponses = sut.handleTemplatingAndCreateConfigs(guid, customizer)

        then:
        boshConfigResponses == null
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        guid   | customizer         | message
        null   | templateCustomizer | "Service Instance GUID cannot be empty!"
        ""     | templateCustomizer | "Service Instance GUID cannot be empty!"
        "test" | null               | "Template customizer cannot be null!"
    }

    @Unroll
    def "should fail to deploy because #message"() {
        when:
        Collection<ServiceDetail> details = sut.handleTemplatingAndCreateDeployment(guid,
                                                                                    templateIdentifier,
                                                                                    parameters,
                                                                                    customizer)

        then:
        details == null
        def exception = thrown(IllegalArgumentException.class)
        exception.message == message

        where:
        guid   | templateIdentifier       | parameters       | customizer         | message
        null   | DEPLOYMENT_TEMPLATE_NAME | EMPTY_PARAMETERS | templateCustomizer | "Service Instance GUID cannot be empty!"
        ""     | DEPLOYMENT_TEMPLATE_NAME | EMPTY_PARAMETERS | templateCustomizer | "Service Instance GUID cannot be empty!"
        "guid" | null                     | EMPTY_PARAMETERS | templateCustomizer | "Template identifier cannot be empty!"
        "guid" | ""                       | EMPTY_PARAMETERS | templateCustomizer | "Template identifier cannot be empty!"
        "guid" | DEPLOYMENT_TEMPLATE_NAME | null             | templateCustomizer | "Parameters cannot be null!"
        "guid" | DEPLOYMENT_TEMPLATE_NAME | EMPTY_PARAMETERS | null               | "Template customizer cannot be null!"
        "guid" | "DOES_NOT_EXIST"         | EMPTY_PARAMETERS | templateCustomizer | "No template could be found for templateIdentifier \"DOES_NOT_EXIST\"!"
    }
}
