package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.util.Resource

class ServiceDefinitionFunctionalSpec extends BaseFunctionalSpec {
    private String serviceName = 'functionalTestServiceforServiceDefiniton'

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist(serviceName, serviceName)
    }

    def "service definition gets created/updated"() {
        given:
        String serviceId = serviceLifeCycler.cfService.guid
        when:
        def response = serviceBrokerClient.createOrUpdateServiceDefinition(Resource.readTestFileContent("/service-data/service1.json"))
        then:
        response.statusCodeValue == 200
    }

    def "service definition delete"() {
        given:
        String serviceId = serviceLifeCycler.cfService.guid
        when:
        def response = serviceBrokerClient.deleteServiceDefinition(serviceId)
        then:
        response.statusCodeValue == 200
    }
}