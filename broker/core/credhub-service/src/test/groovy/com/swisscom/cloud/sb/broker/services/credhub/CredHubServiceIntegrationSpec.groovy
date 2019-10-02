package com.swisscom.cloud.sb.broker.services.credhub

import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.credhub.core.CredHubException
import org.springframework.credhub.support.ClientOptions
import org.springframework.credhub.support.CredentialType
import org.springframework.credhub.support.json.JsonCredential
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.recordSpec
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

class CredHubServiceIntegrationSpec extends Specification {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredHubServiceIntegrationSpec.class)

    public static final String registrationId = "credhub-client"
    OAuth2CredHubService testee

    private static final String UAA_URL = "http://localhost:9091"
    private static final String CREDHUB_URL = "https://localhost:9000"
    @ClassRule
    public static WireMockRule uaaWireMock
    @ClassRule
    public static WireMockRule credhubMock

    private static final RECORD = true

    def setupSpec() {

        WireMockConfiguration uaaWireMockConfiguration = options().
                withRootDirectory("src/test/resources/uaa").
                useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
                .port(19090)
        uaaWireMock = new WireMockRule(uaaWireMockConfiguration)
        uaaWireMock.start()

        WireMockConfiguration credhubWireMockConfiguration = options().
                withRootDirectory("src/test/resources/credhub").
                useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
                .port(19000)
        credhubMock = new WireMockRule(credhubWireMockConfiguration)
        credhubMock.start()

        if (RECORD) {
            LOGGER.info("Start recording with bosh wiremock targeting '..' and uaa wiremock targeting '${UAA_URL}'")

            uaaWireMock.startRecording(recordSpec()
                    .forTarget(UAA_URL)
                    .extractBinaryBodiesOver(10240)
                    .extractTextBodiesOver(256)
                    .captureHeader("Authorization")
                    .makeStubsPersistent(true))
            credhubMock.startRecording(recordSpec()
                    .forTarget(CREDHUB_URL)
                    .extractBinaryBodiesOver(10240)
                    .extractTextBodiesOver(256)
                    .captureHeader("Authorization")
                    .makeStubsPersistent(true))
        }
    }

    def cleanupSpec() {
        if (RECORD) {
            uaaWireMock.stopRecording()
            credhubMock.stopRecording()
        }
        uaaWireMock.stop()
        credhubMock.stop()
    }

    void setup() {
        def clientRegistration = ClientRegistration
            .withRegistrationId(registrationId)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .clientId("credhub_client")
            .clientSecret("secret")
            .tokenUri("http://localhost:19090/uaa/oauth/token")
            .build()
        def clientRegistrationRepository = new InMemoryClientRegistrationRepository(clientRegistration)

        testee = OAuth2CredHubService.of(
                new URI("http://localhost:19000"),
                registrationId,
                new ClientOptions(),
                clientRegistrationRepository,
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository))
    }

    void 'should create credential'() {
        given:
        def name = "1b4ac6f4-e509-11e9-81b4-2a2ae2dbcce4"
        def key = "1b4acc30-e509-11e9-81b4-2a2ae2dbcce4"
        def value =  "1b4acdac-e509-11e9-81b4-2a2ae2dbcce4"

        when:
        def cred = testee.writeCredential(name, [("${key}".toString()): value])

        then:
        noExceptionThrown()
        cred.name.name == name
        cred.value.get(key) == value
        cred.credentialType == CredentialType.JSON

        cleanup:
        testee.deleteCredential(cred.name.name)
    }

    void 'should get credential by id'() {
        given:
        def name = "1b4acee2-e509-11e9-81b4-2a2ae2dbcce4"
        def key = "1b4ad018-e509-11e9-81b4-2a2ae2dbcce4"
        def value =  "1b4ad14e-e509-11e9-81b4-2a2ae2dbcce4"
        def cred = testee.writeCredential(name, [("${key}".toString()): value])

        when:
        def result = testee.getCredential(cred.id)

        then:
        noExceptionThrown()
        result.name.name == name
        result.value.get(key) == value
        result.credentialType == CredentialType.JSON

        cleanup:
        testee.deleteCredential(cred.name.name)
    }

    void 'should get password credential by name'() {
        given:
        def name = "d6d1cf69-f15e-4df9-be52-21b301b206bc"
        def key = "8405c744-b28d-4aaa-947e-14b062b5f7e0"
        def value =  "b1b4de36-60c2-453b-bff9-01cb068a6afd"
        def cred = testee.writeCredential(name, [("${key}".toString()): value])

        when:
        def result = testee.getPasswordCredentialByName(name)

        then:
        noExceptionThrown()
        result.name.name == name
        def jsonCredentials = (JsonCredential)result.value
        jsonCredentials.get(key) == value
        result.credentialType == CredentialType.JSON

        cleanup:
        testee.deleteCredential(cred.name.name)
    }

    void 'should delete credential'() {
        given:
        def name = "1b4ad27a-e509-11e9-81b4-2a2ae2dbcce4"
        def key = "1b4ad57c-e509-11e9-81b4-2a2ae2dbcce4"
        def value =  "1b4ad6bc-e509-11e9-81b4-2a2ae2dbcce4"
        def cred = testee.writeCredential(name, [("${key}".toString()): value])

        when:
        testee.deleteCredential(cred.name.name)

        then:
        noExceptionThrown()

        when:
        def result = testee.getCredential(cred.id)

        then:
        def ex = thrown(CredHubException)
        ex.message =~ "404"
        result == null
    }

    void 'should add and get read permission for an app'() {
        given:
        def name = "8477355d-ca8a-4790-9ddf-f72e388f7b38"
        def key = "7ab17691-8c84-4970-98f3-c15d18cac4a2"
        def value =  "5a7a03e5-2588-4421-9cc2-8199601beef9"
        def cred = testee.writeCredential(name, [("${key}".toString()): value])
        def appUuid = "d6e33ae1-6b46-4452-b3e2-ab664834dccb"

        when:
        testee.addReadPermission(cred.name.name, appUuid)

        then:
        noExceptionThrown()
        def permissions = testee.getPermissions(cred.name.name)
        permissions.size() >= 1

        cleanup:
        testee.deletePermission(cred.name.name, appUuid)
        testee.deleteCredential(cred.name.name)
    }

    void 'should delete permission for an app'() {
        given:
        def name = "d0c80776-bcf7-45e0-8737-b48ad688d7fb"
        def key = "0cd1c805-6541-4b7a-8f3f-fc9c32c93d31"
        def value =  "39374b3f-81f4-41d2-96e4-ed720282152c"
        def cred = testee.writeCredential(name, [("${key}".toString()): value])
        def appUuid = "fcd7fa96-319b-4f2b-9fb8-38c617fbaf3d"
        testee.addReadPermission(name, appUuid)

        when:
        testee.deletePermission(name, appUuid)

        then:
        noExceptionThrown()
        def permissions = testee.getPermissions(name)
        permissions.size() == 0

        cleanup:
        testee.deleteCredential(cred.name.name)
    }
}
