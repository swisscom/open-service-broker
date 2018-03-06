package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.DummyConfig
import com.swisscom.cloud.sb.broker.util.HttpHelper
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.RequestMatcher
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

class BoshRestClientSpec extends Specification {
    MockRestServiceServer mockServer
    BoshRestClient boshRestClient
    RestTemplateBuilder restTemplateBuilder
    String username = 'username'
    String password = 'password'

    def setup() {
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        restTemplateBuilder = Mock(RestTemplateBuilder)
        restTemplateBuilder.withSSLValidationDisabled() >> restTemplateBuilder
        restTemplateBuilder.build() >> restTemplate

        and:
        boshRestClient = new BoshRestClient(new DummyConfig(boshDirectorBaseUrl: '',
                boshDirectorUsername: username,
                boshDirectorPassword: password), restTemplateBuilder)
    }


    def "GetBoshInfo"() {
        given:
        def response = '{"status":"status"}'
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.INFO)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        def result = boshRestClient.fetchBoshInfo()
        then:
        mockServer.verify()
        result == response
    }

    def "GetDeployment"() {
        given:
        def response = 'someDeployment'
        def deploymentId = 'deploymentId'
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.DEPLOYMENTS + "/${deploymentId}")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(autHeader())
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        def result = boshRestClient.getDeployment(deploymentId)
        then:
        mockServer.verify()
        result == response
    }

    def "happy path: PostDeployment"() {
        given:
        def response = '/1'
        def data = 'data'
        def redirectLocation = 'http://localhost/' + BoshRestClient.TASKS + '/1'
        and:
        def headers = new HttpHeaders()
        headers.setLocation(new URI(redirectLocation))
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.DEPLOYMENTS)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(BoshRestClient.CONTENT_TYPE_YAML))
                .andExpect(autHeader())
                .andRespond(withStatus(HttpStatus.FOUND).headers(headers))

        when:
        def result = boshRestClient.postDeployment(data)
        then:
        mockServer.verify()
        result == response
    }

    def "DeleteDeployment"() {
        given:
        def response = '/1'
        def id = 'ud'
        def redirectLocation = 'http://localhost/' + BoshRestClient.TASKS + '/1'
        and:
        def headers = new HttpHeaders()
        headers.setLocation(new URI(redirectLocation))
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.DEPLOYMENTS) + "/${id}" + "?force=true"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(autHeader())
                .andRespond(withStatus(HttpStatus.FOUND).headers(headers))

        when:
        def result = boshRestClient.deleteDeployment(id)
        then:
        mockServer.verify()
        result == response
    }

    def "GetCloudConfig"() {
        given:
        def response = 'response'
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.CLOUD_CONFIGS + BoshRestClient.CLOUD_CONFIG_QUERY)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(autHeader())
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        def result = boshRestClient.fetchCloudConfig()
        then:
        mockServer.verify()
        result == response
    }

    def "PostCloudConfig"() {
        given:
        def data = 'data'
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.CLOUD_CONFIGS)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(autHeader())
                .andExpect(content().contentType(BoshRestClient.CONTENT_TYPE_YAML))
                .andRespond(withSuccess())

        when:
        boshRestClient.postCloudConfig(data)
        then:
        mockServer.verify()
    }

    def "GetTask"() {
        given:
        def response = 'response'
        def taskId = 'taskId'
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.TASKS + "/${taskId}")))
                .andExpect(autHeader())
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))

        when:
        def result = boshRestClient.getTask(taskId)
        then:
        mockServer.verify()
        result == response
    }

    private RequestMatcher autHeader() {
        return header(HttpHeaders.AUTHORIZATION, HttpHelper.computeBasicAuth(username, password))
    }
}
