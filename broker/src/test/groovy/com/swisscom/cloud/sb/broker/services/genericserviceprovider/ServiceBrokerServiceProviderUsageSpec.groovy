package com.swisscom.cloud.sb.broker.services.genericserviceprovider

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
