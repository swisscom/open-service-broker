package com.swisscom.cloud.sb.broker.servicedefinition

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import spock.lang.Specification

class ServiceDefinitionInitializerUnitSpec extends Specification {
    private final String TEST_GUID = "TEST_GUID"

    private ServiceDefinitionConfig serviceDefinitionConfig
    private ServiceDefinitionInitializer serviceDefinitionInitializer
    private CFServiceRepository cfServiceRepository
    private ServiceDefinitionProcessor serviceDefinitionProcessor

    private List<CFService> cfServiceList

    def setup() {
        cfServiceList = [new CFService(guid: TEST_GUID)]
        cfServiceRepository = Mock(CFServiceRepository)
        serviceDefinitionConfig = new ServiceDefinitionConfig(serviceDefinitions: [new ServiceDto(guid: TEST_GUID)])
        serviceDefinitionProcessor = new ServiceDefinitionProcessor(cfServiceRepository: cfServiceRepository)
        serviceDefinitionInitializer = new ServiceDefinitionInitializer(cfServiceRepository, serviceDefinitionConfig, serviceDefinitionProcessor)
    }

    def "Matching service definitions"() {
        when:
        serviceDefinitionInitializer.checkForMissingServiceDefinitions(cfServiceList)

        then:
        noExceptionThrown()
    }

    def "Adding service definition from config"() {
        given:
        serviceDefinitionConfig.serviceDefinitions << new ServiceDto(name: "test3")

        when:
        serviceDefinitionInitializer.checkForMissingServiceDefinitions(cfServiceList)

        then:
        noExceptionThrown()
    }

    def "Missing service definition from config"() {
        given:
        cfServiceList << new CFService(guid: "TEST_GUID2")

        when:
        serviceDefinitionInitializer.checkForMissingServiceDefinitions(cfServiceList)

        then:
        def exception = thrown(Exception)
        exception.message == "Missing service definition configuration exception. Service list - [TEST_GUID, TEST_GUID2]"
    }

    def "Update service definition"() {
        given:
        CFService cfService = new CFService(guid: TEST_GUID)
        cfServiceRepository.findByGuid(serviceDefinitionConfig.serviceDefinitions[0].guid) >> cfService
        cfServiceRepository.save(cfService) >> cfService

        when:
        serviceDefinitionInitializer.addServiceDefinitions()

        then:
        noExceptionThrown()
    }
}
