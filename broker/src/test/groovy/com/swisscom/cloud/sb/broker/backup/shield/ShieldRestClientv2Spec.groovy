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

package com.swisscom.cloud.sb.broker.backup.shield


import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClientv1
import com.swisscom.cloud.sb.broker.backup.shield.restClient.ShieldRestClientv2
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import spock.lang.Ignore
import spock.lang.Specification

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

class ShieldRestClientv2Spec extends Specification {
    ShieldRestClientv2 shieldRestClient
    MockRestServiceServer mockServer
    ShieldConfig shieldConfig

    class DummyTarget implements ShieldTarget {

        @Override
        String pluginName() {
            "doesntmatter"
        }

        @Override
        String endpointJson() {
            "{}"
        }
    }

    def setup() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()

        MockRestServiceServer initMockServer = MockRestServiceServer.createServer(restTemplateBuilder.build())
        shieldConfig = new ShieldConfig()
        shieldConfig.baseUrl = "http://baseurl"
        shieldConfig.username = "admin"
        shieldConfig.password = "shield"
        shieldConfig.defaultTenantName = "tenant1"
        shieldConfig.apiKey = "apiKey"
        and:
        initMockServer.expect(requestTo(shieldConfig.baseUrl + "/v1/status"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(ShieldRestClientv1.HEADER_API_KEY, shieldConfig.apiKey))
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andRespond(withSuccess('{"version":"1.0"}', MediaType.APPLICATION_JSON))
        shieldRestClient = new ShieldRestClientv2(shieldConfig, restTemplateBuilder)
        mockServer = MockRestServiceServer.createServer(restTemplateBuilder.build())
    }

    def "check if parseAndCheckVersion works as expected"() {
        given:
        String body = '{"ip":"172.19.0.8","env":"sandbox","color":"yellow","motd":"Welcome to SHIELD!\\n","api":2}'

        when:
        Boolean matched = shieldRestClient.parseAndCheckVersion(body)

        then:
        matched
    }
}