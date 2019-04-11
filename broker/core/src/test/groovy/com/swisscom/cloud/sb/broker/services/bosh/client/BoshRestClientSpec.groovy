/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.DummyConfig
import com.swisscom.cloud.sb.broker.services.bosh.dto.BoshConfigRequestDto
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
        boshRestClient = Spy(BoshRestClient, constructorArgs: [new DummyConfig(boshDirectorBaseUrl: '',
                boshDirectorUsername: username,
                boshDirectorPassword: password), restTemplateBuilder])
        boshRestClient.checkAuthTypeAndLogin() >> null
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

    def "Login to UAA"() {
        given:
        String expectedToken = "eyJhbGciOiSldUIn0eyJqdGkiOiI0MGZhMmY4NjY5Y2E0YzNjY"
        def response = """{"access_token": "${expectedToken}",
                          "token_type": "bearer",
                          "expires_in": 41199,
                          "scope": "clients.read password.write clients.secret clients.write uaa.admin scim.write scim.read",
                          "jti": "fadfwe235wfdafawfewaf43fewf2"
                        }"""
        mockServer.expect(requestTo("https://localhost" + BoshRestClient.OAUTH_TOKEN + "?grant_type=client_credentials"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON))
        when:
        String responseToken = boshRestClient.uaaLogin("https://localhost")
        then:
        mockServer.verify()
        responseToken == expectedToken
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

    def "happy path: PostConfig"() {
        given:
        BoshConfigRequestDto configRequestDto = new BoshConfigRequestDto(name: 'test', type: 'cloud', content: '---')
        and:
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.CONFIGS)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(BoshRestClient.CONTENT_TYPE_JSON))
                .andExpect(autHeader())
                .andRespond(withStatus(HttpStatus.OK))

        when:
        boshRestClient.postConfig(configRequestDto.toJson())
        then:
        mockServer.verify()
    }

    def "happy path: DeleteConfig"() {
        given:
        String name = 'test'
        String type = 'cloud'
        and:
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.CONFIGS) + "?name=${name}&type=${type}"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(autHeader())
                .andRespond(withStatus(HttpStatus.OK))

        when:
        boshRestClient.deleteConfig(name, type)
        then:
        mockServer.verify()
    }

    def "happy path: GetConfigs"() {
        given:
        String name = 'test'
        String type = 'cloud'
        and:
        mockServer.expect(requestTo(boshRestClient.prependBaseUrl(BoshRestClient.CONFIGS) + "?name=${name}&type=${type}&latest=true"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(autHeader())
                .andRespond(withStatus(HttpStatus.OK))

        when:
        def response = boshRestClient.getConfigs(name, type)
        then:
        mockServer.verify()
    }

    private RequestMatcher autHeader() {
        return header(HttpHeaders.AUTHORIZATION, HttpHelper.computeBasicAuth(username, password))
    }
}
