package com.swisscom.cf.broker.functional

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cf.broker.cfextensions.endpoint.EndpointDto
import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import com.swisscom.cf.broker.util.ServiceLifeCycler
import com.swisscom.cf.broker.util.test.DummyServiceProvider
import com.swisscom.cf.broker.util.test.DummySynchronousServiceProvider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

import static com.swisscom.cf.broker.util.HttpHelper.createSimpleAuthHeaders


class EndpointLookupFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyServiceManagerBased', ServiceProviderLookup.findInternalName(DummyServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "should get a non empty response for endpoints of a service manager based service"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(false, false)

        when:
        def response = getEndpoint(serviceLifeCycler.serviceInstanceId)
        then:
        response.statusCode.'2xxSuccessful'
        List endpoints = new ObjectMapper().readValue(response.body, new TypeReference<List<EndpointDto>>() {})
        endpoints.size() > 0

        cleanup:
        serviceLifeCycler.deleteServiceInstanceAndAssert(false)
    }

    def "should get an empty response for a *NON* service manager based service"() {
        given:
        ServiceLifeCycler lifeCycler = applicationContext.getBean(ServiceLifeCycler.class)
        lifeCycler.createServiceIfDoesNotExist('SynchronousDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
        lifeCycler.createServiceInstanceAndAssert(false, false)
        when:
        def response = getEndpoint(lifeCycler.serviceInstanceId)
        then:
        response.statusCode.'2xxSuccessful'
        List endpoints = new ObjectMapper().readValue(response.body, new TypeReference<List<EndpointDto>>() {})
        endpoints.size() == 0

        cleanup:
        lifeCycler.deleteServiceInstanceAndAssert(false)
        lifeCycler.cleanup()
    }

    private ResponseEntity getEndpoint(String serviceInstanceGuid) {
        new RestTemplate().exchange(cfExtEndpointUrl,HttpMethod.GET,new HttpEntity(createSimpleAuthHeaders(cfExtUser,cfExtPassword)) , String.class, serviceInstanceGuid)
    }
}