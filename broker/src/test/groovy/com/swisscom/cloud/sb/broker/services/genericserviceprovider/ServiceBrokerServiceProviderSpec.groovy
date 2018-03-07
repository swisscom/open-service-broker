package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import com.swisscom.cloud.sb.client.ServiceBrokerClient
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class ServiceBrokerServiceProviderSpec extends Specification{

    private ServiceBrokerServiceProvider serviceBrokerServiceProvider
    private Plan syncPlan
    private Plan asyncPlan

    private RestTemplateBuilder restTemplateBuilder
    private MockRestServiceServer mockServer
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository
    private ServiceBrokerClient serviceBrokerClient

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)

        and:
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        serviceBrokerClient = new ServiceBrokerClient(restTemplate, "http://dummy", "dummy", "dummy");

        and:
        syncPlan = new Plan(asyncRequired: false, parameters: [new Parameter(name: "baseUrl", value: "http://dummy"), new Parameter(name: "username", value: "dummy"), new Parameter(name: "password", value: "dummy"), new Parameter(name: "service-guid", value: "dummy"), new Parameter(name: "plan-guid", value: "dummy")])
        asyncPlan = new Plan(asyncRequired: true, parameters: [new Parameter(name: "baseUrl", value: "http://dummy"), new Parameter(name: "username", value: "dummy"), new Parameter(name: "password", value: "dummy"), new Parameter(name: "service-guid", value: "dummy"), new Parameter(name: "plan-guid", value: "dummy")])
        serviceBrokerServiceProvider = new ServiceBrokerServiceProvider(serviceBrokerClient)
    }

    def "provision sync service instance with sync client"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(acceptsIncomplete: false, serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: syncPlan)

        mockServer.expect(MockRestRequestMatchers.requestTo("http://dummy/v2/service_instances/${provisionRequest.serviceInstanceGuid}?accepts_incomplete=${provisionRequest.acceptsIncomplete}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess())

        when:
        def provisionResponse = serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        provisionResponse.isAsync == false
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "deprovision sync service instance with sync client"() {
        given:
        def serviceId = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, plan: syncPlan)
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(acceptsIncomplete: false, serviceInstanceGuid: serviceId, serviceInstance: serviceInstance)
        def url = "http://dummy/v2/service_instances/${deprovisionRequest.serviceInstanceGuid}?service_id=dummy&plan_id=dummy&accepts_incomplete=${syncPlan.asyncRequired}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def deprovisionResponse = serviceBrokerServiceProvider.deprovision(deprovisionRequest)

        then:
        deprovisionResponse.isAsync == false
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "provision async service instance with async client"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(acceptsIncomplete: true, serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: asyncPlan)

        mockServer.expect(MockRestRequestMatchers.requestTo("http://dummy/v2/service_instances/${provisionRequest.serviceInstanceGuid}?accepts_incomplete=${provisionRequest.plan.asyncRequired}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess())

        when:
        def provisionResponse = serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        provisionResponse.isAsync == true
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "deprovision async service instance with async client"() {
        given:
        def serviceId = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, plan: asyncPlan)
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(acceptsIncomplete: true, serviceInstanceGuid: serviceId, serviceInstance: serviceInstance)

        def url = "http://dummy/v2/service_instances/${deprovisionRequest.serviceInstanceGuid}?service_id=dummy&plan_id=dummy&accepts_incomplete=${asyncPlan.asyncRequired}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def deprovisionResponse = serviceBrokerServiceProvider.deprovision(deprovisionRequest)

        then:
        deprovisionResponse.isAsync == true
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "provision sync service instance with async client"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(acceptsIncomplete: true, serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: syncPlan)

        mockServer.expect(MockRestRequestMatchers.requestTo("http://dummy/v2/service_instances/${provisionRequest.serviceInstanceGuid}?accepts_incomplete=${provisionRequest.plan.asyncRequired}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess())

        when:
        def provisionResponse = serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        provisionResponse.isAsync == false
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "deprovision sync service instance with async client"() {
        given:
        def serviceId = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, plan: syncPlan)
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(acceptsIncomplete: true, serviceInstanceGuid: serviceId, serviceInstance: serviceInstance)

        def url = "http://dummy/v2/service_instances/${deprovisionRequest.serviceInstanceGuid}?service_id=dummy&plan_id=dummy&accepts_incomplete=${deprovisionRequest.acceptsIncomplete}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def deprovisionResponse = serviceBrokerServiceProvider.deprovision(deprovisionRequest)

        then:
        deprovisionResponse.isAsync == true
        noExceptionThrown()

        and:
        mockServer.verify()
    }
}
