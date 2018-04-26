package com.swisscom.cloud.sb.broker.services.lapi

import com.swisscom.cloud.sb.broker.binding.BindRequest
import com.swisscom.cloud.sb.broker.binding.UnbindRequest
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.lapi.config.LapiConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class LapiServiceProviderSpec extends Specification {

    private LapiServiceProvider lapiServiceProvider
    private LapiConfig lapiConfig

    private MockRestServiceServer mockServer
    private ServiceInstanceRepository serviceInstanceRepository
    private ServiceBindingRepository serviceBindingRepository
    private RestTemplateBuilder restTemplateBuilder

    def setup() {
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        serviceBindingRepository = Mock(ServiceBindingRepository)

        and:
        RestTemplate restTemplate = new RestTemplate()
        lapiConfig = new LapiConfig()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        restTemplateBuilder = Mock(RestTemplateBuilder)
        restTemplateBuilder.build() >> restTemplate

        and:
        restTemplateBuilder.withBasicAuthentication(_, _) >> restTemplateBuilder

        and:
        lapiServiceProvider = new LapiServiceProvider(restTemplateBuilder, lapiConfig)
    }

    def "provision service instance"() {
        given:
        ProvisionRequest provisionRequest = new ProvisionRequest(serviceInstanceGuid: "65d546f1-2c74-4871-9d5f-b5b0df1a7082", plan: new Plan())

        mockServer.expect(MockRestRequestMatchers.requestTo("http://0.0.0.0:4567/v2/service_instances/${provisionRequest.serviceInstanceGuid}"))
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
        //ServiceInstance serviceInstance = new ServiceInstance()
        //serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        def url = "http://0.0.0.0:4567/v2/service_instances/${serviceId}"

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

    def "bind to service instance"() {
        given:
        String serviceBindingId = "serviceBindingId"
        def serviceId = "serviceId"
        ServiceInstance serviceInstance = new ServiceInstance(guid: serviceId)
        BindRequest bindRequest = new BindRequest(serviceInstance: serviceInstance, binding_guid: serviceBindingId)
        serviceInstanceRepository.findByGuid(serviceId) >> serviceInstance

        String url = "http://0.0.0.0:4567/v2/service_instances/${serviceId}/service_bindings/${serviceBindingId}"

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

        String url = "http://0.0.0.0:4567/v2/service_instances/${unbindRequest.serviceInstance.guid}/service_bindings/${serviceBinding.guid}"

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
}