package com.swisscom.cloud.sb.broker.services.lapi

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class LapiServiceProviderSpec extends Specification{

    private LapiServiceProvider lapiServiceProvider

    private MockRestServiceServer mockServer
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository
    private RestTemplateBuilder restTemplateBuilder

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)

        and:
        RestTemplate restTemplate = new RestTemplate()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        restTemplateBuilder = Mock(RestTemplateBuilder)
        restTemplateBuilder.build() >> restTemplate

        and:
        restTemplateBuilder.withBasicAuthentication(_, _) >> restTemplateBuilder

        and:
        lapiServiceProvider = new LapiServiceProvider(restTemplateBuilder)
    }

    def "provision service instance"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: new Plan())

        mockServer.expect(MockRestRequestMatchers.requestTo("http://0.0.0.0:4567/v2/service-instances/${provisionRequest.serviceInstanceGuid}"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess())

        when:
        def provisionResponse = lapiServiceProvider.provision(provisionRequest)

        then:
        provisionResponse.isAsync == false
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    def "deprovision service instance"() {
        given:
        def serviceId = "65d546f1-2c74-4871-9d5f-b5b0df1a7082"
        DeprovisionRequest deprovisionRequest = new DeprovisionRequest(serviceInstanceGuid: serviceId)
        ServiceInstance serviceInstance = new ServiceInstance()
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        def url = "http://0.0.0.0:4567/v2/service-instances/${serviceId}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        def deprovisionResponse = lapiServiceProvider.deprovision(deprovisionRequest)

        then:
        deprovisionResponse.isAsync == false
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    //bindingId in bindRequest params?
    def "bind to service instance"() {
        given:
        String serviceBindingId = "serviceBindingId"
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId)
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: ["serviceBindingId": serviceBindingId])
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        String url = "http://0.0.0.0:4567/v2/service-instances/${serviceId}/service-bindings/${serviceBindingId}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        lapiServiceProvider.bind(bindRequest)

        then:
        mockServer.verify()
    }

    def "unbind from service instance"() {
        given:
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId)
        String serviceBindingId = "serviceBindingId"
        ServiceBinding serviceBinding = new ServiceBinding(guid: serviceBindingId)
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, serviceInstance: serviceInstance)
        serviceBindingRepository.findByGuid(serviceBindingId) >> serviceBinding

        String url = "http://0.0.0.0:4567/v2/service-instances/${unbindRequest.serviceInstance.guid}/service-bindings/${serviceBinding.guid}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        when:
        lapiServiceProvider.unbind(unbindRequest)

        then:
        noExceptionThrown()

        and:
        mockServer.verify()
    }

    //how to test this considering no actual entry is made? Does not detect that it already exists...
    //not implemented on LAPI side, yet
    def "provision already existing service"() {

            given:
            ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: new Plan())

            mockServer.expect(MockRestRequestMatchers.requestTo("http://0.0.0.0:4567/v2/service-instances/${provisionRequest.serviceInstanceGuid}"))
                    .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                    .andRespond(MockRestResponseCreators.withSuccess())

            mockServer.expect(MockRestRequestMatchers.requestTo("http://0.0.0.0:4567/v2/service-instances/${provisionRequest.serviceInstanceGuid}"))
                    .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                    .andRespond(MockRestResponseCreators.withBadRequest())

            when:
            def provisionResponse = lapiServiceProvider.provision(provisionRequest)

            then:
            thrown(HttpServerErrorException)

            and:
            mockServer.verify()
    }

    //not implemented on LAPI side, yet
    /*def "bind already existing binding to service instance"() {
        given:
        String serviceBindingId = "serviceBindingId"
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId)
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, parameters: ["serviceBindingId": serviceBindingId])
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        String url = "http://0.0.0.0:4567/v2/service-instances/${serviceId}/service-bindings/${serviceBindingId}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.CONFLICT))

        when:
        lapiServiceProvider.bind(bindRequest)

        then:
        thrown()

        and:
        mockServer.verify()
    }

    //not implemented on LAPI side, yet
    def "unbind non-existing binding from service instance"() {
        given:
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId)
        String serviceBindingId = "serviceBindingId"
        ServiceBinding serviceBinding = new ServiceBinding(guid: serviceBindingId)
        UnbindRequest unbindRequest = new UnbindRequest(binding: serviceBinding, serviceInstance: serviceInstance)
        serviceBindingRepository.findByGuid(serviceBindingId) >> serviceBinding

        String url = "http://0.0.0.0:4567/v2/service-instances/${unbindRequest.serviceInstance.guid}/service-bindings/${serviceBinding.guid}"

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withSuccess("", MediaType.TEXT_PLAIN))

        mockServer.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.GONE))

        when:
        lapiServiceProvider.unbind(unbindRequest)

        then:
        thrown()

        and:
        mockServer.verify()
    }*/
}