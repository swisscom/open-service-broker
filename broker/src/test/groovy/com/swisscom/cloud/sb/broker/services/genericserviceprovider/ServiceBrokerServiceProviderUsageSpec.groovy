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

package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.config.UserConfig
import com.swisscom.cloud.sb.broker.config.WebSecurityConfig
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import org.springframework.http.HttpMethod
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class ServiceBrokerServiceProviderUsageSpec extends Specification {

    private ServiceBrokerServiceProvider serviceBrokerServiceProvider
    private ServiceBrokerServiceProviderUsage serviceBrokerServiceProviderUsage
    private MockRestServiceServer mockServer
    private CFService service
    private Plan syncPlan
    private ServiceBrokerClientExtended serviceBrokerClientExtended
    private ApplicationUserConfig applicationUserConfig

    def setup() {
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        service = new CFService(guid: "dummyService")
        syncPlan = new Plan(guid: "dummyPlan", asyncRequired: false, service: service, parameters: [new Parameter(name: "baseUrl", value: "http://dummy"), new Parameter(name: "username", value: "dummy"), new Parameter(name: "password", value: "dummy"), new Parameter(name: "service-guid", value: "dummy"), new Parameter(name: "plan-guid", value: "dummy")])

        applicationUserConfig = new ApplicationUserConfig()
        applicationUserConfig.platformUsers = new ArrayList<UserConfig>()
        def userConfig = new UserConfig(username: "newUser", platformId: "new-id", password: "newPassword", role: WebSecurityConfig.ROLE_CF_EXT_ADMIN)
        applicationUserConfig.platformUsers.add(userConfig)

        serviceBrokerClientExtended = new ServiceBrokerClientExtended(restTemplate, "http://dummy", "dummy", "dummy", "dummy", "dummy");
        serviceBrokerServiceProviderUsage = new ServiceBrokerServiceProviderUsage(applicationUserConfig, serviceBrokerClientExtended)
        serviceBrokerServiceProvider = new ServiceBrokerServiceProvider(serviceBrokerClientExtended, serviceBrokerServiceProviderUsage)
    }

    def "requestUsage"() {
        given:
        def serviceInstanceGuid = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceGuid, plan: syncPlan)

        when:
        mockServer.expect(MockRestRequestMatchers.requestTo("http://dummy/custom/service_instances/${serviceInstanceGuid}/usage"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess())

        and:
        def serviceUsage = serviceBrokerServiceProvider.findUsage(serviceInstance, null)

        then:
        serviceUsage.value == null
        noExceptionThrown()

        and:
        mockServer.verify()
    }

}
