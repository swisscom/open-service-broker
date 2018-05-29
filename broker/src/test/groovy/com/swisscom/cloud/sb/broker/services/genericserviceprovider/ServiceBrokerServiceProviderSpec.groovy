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

class ServiceBrokerServiceProviderSpec extends Specification{

    private ServiceBrokerServiceProvider serviceBrokerServiceProvider
    private CFService service
    private Plan syncPlan
    private Plan asyncPlan

    private MockRestServiceServer mockServer
    private ServiceBrokerClient serviceBrokerClient

    def setup() {
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        serviceBrokerClient = new ServiceBrokerClient(restTemplate, "http://dummy", "dummy", "dummy")

        and:
        service = new CFService(guid: "dummyService")

        and:
        syncPlan = new Plan(guid: "dummyPlan", asyncRequired: false, service: service, parameters: [new Parameter(name: "baseUrl", value: "http://dummy"), new Parameter(name: "username", value: "dummy"), new Parameter(name: "password", value: "dummy"), new Parameter(name: "service-guid", value: "dummy"), new Parameter(name: "plan-guid", value: "dummy")])
        asyncPlan = new Plan(guid: "dummyPlan", asyncRequired: true, service: service, parameters: [new Parameter(name: "baseUrl", value: "http://dummy"), new Parameter(name: "username", value: "dummy"), new Parameter(name: "password", value: "dummy"), new Parameter(name: "service-guid", value: "dummy"), new Parameter(name: "plan-guid", value: "dummy")])
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
        !provisionResponse.isAsync
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "deprovision sync service instance with sync client"() {
        given:
        def serviceId = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, plan: syncPlan)
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(acceptsIncomplete: false, serviceInstanceGuid: serviceId, serviceInstance: serviceInstance)
        def url = "http://dummy/v2/service_instances/${deprovisionRequest.serviceInstanceGuid}?service_id=dummy&plan_id=dummy&accepts_incomplete=${deprovisionRequest.acceptsIncomplete}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def deprovisionResponse = serviceBrokerServiceProvider.deprovision(deprovisionRequest)

        then:
        !deprovisionResponse.isAsync
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "provision sync service instance with async client"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(acceptsIncomplete: true, serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: syncPlan)

        mockServer.expect(MockRestRequestMatchers.requestTo("http://dummy/v2/service_instances/${provisionRequest.serviceInstanceGuid}?accepts_incomplete=${provisionRequest.acceptsIncomplete}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess())

        when:
        def provisionResponse = serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        !provisionResponse.isAsync
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

        !deprovisionResponse.isAsync
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "provision async service instance with sync client"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(acceptsIncomplete: false, serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: asyncPlan)

        when:
        serviceBrokerServiceProvider.provision(provisionRequest)

        then:
        ServiceBrokerException e = thrown()
        ErrorCodeHelper.assertServiceBrokerException(ErrorCode.ASYNC_REQUIRED, e)

    }

    def "bind to sync service instance"() {
        given:
        String serviceBindingId = "serviceBindingId"
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, plan: syncPlan)
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, binding_guid: serviceBindingId, plan: syncPlan, service: service)

        String url = "http://dummy/v2/service_instances/${serviceId}/service_bindings/${serviceBindingId}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        serviceBrokerServiceProvider.bind(bindRequest)

        then:
        mockServer.verify()
    }

    def "bind to async service instance"() {
        given:
        String serviceBindingId = "serviceBindingId"
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId, plan: asyncPlan)
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, binding_guid: serviceBindingId, plan: asyncPlan, service: service)

        String url = "http://dummy/v2/service_instances/${serviceId}/service_bindings/${serviceBindingId}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        serviceBrokerServiceProvider.bind(bindRequest)

        then:
        mockServer.verify()
    }

    def "unbind from sync service instance"() {
        given:
        def serviceInstanceId = "serviceInstanceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceId, plan: syncPlan)
        String serviceBindingId = "serviceBindingId"
        ServiceBinding serviceBinding = new ServiceBinding(guid: serviceBindingId)
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, serviceInstance: serviceInstance, service: service)

        String url = "http://dummy/v2/service_instances/${unbindRequest.serviceInstance.guid}/service_bindings/${unbindRequest.binding.guid}?service_id=dummyService&plan_id=${serviceInstance.plan.guid}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        serviceBrokerServiceProvider.unbind(unbindRequest)

        then:
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "unbind from async service instance"() {
        given:
        def serviceInstanceId = "serviceInstanceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceInstanceId, plan: asyncPlan)
        String serviceBindingId = "serviceBindingId"
        ServiceBinding serviceBinding = new ServiceBinding(guid: serviceBindingId)
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, serviceInstance: serviceInstance, service: service)

        String url = "http://dummy/v2/service_instances/${unbindRequest.serviceInstance.guid}/service_bindings/${unbindRequest.binding.guid}?service_id=dummyService&plan_id=${serviceInstance.plan.guid}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        serviceBrokerServiceProvider.unbind(unbindRequest)

        then:
        noExceptionThrown()

        and:
        mockServer.verify()
    }
}
