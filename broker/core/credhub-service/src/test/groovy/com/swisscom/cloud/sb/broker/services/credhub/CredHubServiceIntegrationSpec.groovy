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

    private static final String UAA_URL = "http://localhost:9090"
    private static final String CREDHUB_URL = "https://localhost:9000"
    @ClassRule
    public static WireMockRule uaaWireMock
    @ClassRule
    public static WireMockRule credhubMock

    private static final RECORD = false

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

    void 'should get credential'() {
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
}
