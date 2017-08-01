package com.swisscom.cloud.sb.broker.services.openwhisk

import com.fasterxml.jackson.databind.JsonNode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class OpenWhiskServiceProviderSpec extends Specification{
    private final String NAMESPACE = "TEST_NAMESPACE"
    private final String SUBJECT = "TEST_SUBJECT"
    private final String UUID = "TEST_UUID"
    private final String KEY = "TEST_KEY"

    OpenWhiskServiceProvider openWhiskServiceProvider
    OpenWhiskConfig openWhiskConfig
    RestTemplateFactory restTemplateFactory
    MockRestServiceServer mockServer

    def setup() {
        restTemplateFactory = Mock(RestTemplateFactory)
        RestTemplate restTemplate = new RestTemplate()
        restTemplateFactory.buildWithBasicAuthentication(_,_) >> restTemplate
        mockServer = MockRestServiceServer.createServer(restTemplate)
        openWhiskConfig = new OpenWhiskConfig(openWhiskDbProtocol: "http",
                openWhiskDbHost: "openwhiskHost", openWhiskDbPort: "1234", openWhiskDbLocalUser: "ubuntu",
                openWhiskDbHostname: "localhost")
        and:
        openWhiskServiceProvider = new OpenWhiskServiceProvider(openWhiskConfig, restTemplateFactory,
                new OpenWhiskDbClient(openWhiskConfig, restTemplateFactory))
    }

    def "Verify creating a new subject"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        JsonNode res = openWhiskServiceProvider.subjectHelper(NAMESPACE, SUBJECT, UUID, KEY)

        then:
        res.path("subject").asText().trim() == SUBJECT
        res.path("namespaces").path(0).path("name").asText().trim() == NAMESPACE
        res.path("namespaces").path(0).path("uuid").asText().trim() == UUID
        res.path("namespaces").path(0).path("key").asText().trim() == KEY

        and:
        mockServer.verify()
    }

    def "Verify adding a namespace to subject"() {
        given:
        def response = """{"_id":"TEST_SUBJECT","_rev":"rev","subject":"TEST_SUBJECT","namespaces":[]}"""
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.TEXT_PLAIN))

        when:
        JsonNode res = openWhiskServiceProvider.subjectHelper(NAMESPACE, SUBJECT, UUID, KEY)

        then:
        res.path("subject").asText().trim() == SUBJECT
        res.path("namespaces").path(0).path("name").asText().trim() == NAMESPACE
        res.path("namespaces").path(0).path("uuid").asText().trim() == UUID
        res.path("namespaces").path(0).path("key").asText().trim() == KEY

        and:
        mockServer.verify()
    }

    def "Verify namespace conflict within subject"() {
        given:
        def response = """{"_id":"TEST_SUBJECT","_rev":"rev","subject":"TEST_SUBJECT","namespaces":[{"name":"TEST_NAMESPACE","uuid":"TEST_UUID","key":"TEST_KEY"}]}"""
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.TEXT_PLAIN))

        when:
        JsonNode res = openWhiskServiceProvider.subjectHelper(NAMESPACE, SUBJECT, UUID, KEY)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.CONFLICT

        and:
        mockServer.verify()
    }

    def "Verify delete entity with existing subject/namespace"() {
        given:
        def rev = "1-123456"
        def response = """{"_id":"TEST_SUBJECT","_rev":"${rev}","subject":"TEST_SUBJECT","namespaces":[]}"""
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/${SUBJECT}?rev=${rev}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess(response, MediaType.TEXT_PLAIN))

        when:
        openWhiskServiceProvider.deleteEntity(SUBJECT)

        then:
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "Verify delete entity without existing subject/namespace"() {
        given:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://openwhiskHost:1234/ubuntu_localhost_subjects/" + SUBJECT))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST))

        when:
        openWhiskServiceProvider.deleteEntity(SUBJECT)

        then:
        def exception = thrown(ServiceBrokerException)
        exception.httpStatus == HttpStatus.BAD_REQUEST

        and:
        mockServer.verify()
    }

}
