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
        serviceDefinitionInitializer.addOrUpdateServiceDefinitions()

        then:
        noExceptionThrown()
    }
}
