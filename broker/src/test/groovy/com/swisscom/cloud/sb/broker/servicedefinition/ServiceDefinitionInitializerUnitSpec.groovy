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
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.PlanMetadata
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.ParameterRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanMetadataRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import spock.lang.Specification

class ServiceDefinitionInitializerUnitSpec extends Specification {
    private final String TEST_GUID = "TEST_GUID"
    private final String TEST_GUID2 = "TEST_GUID2"
    private final String TEST_GUID3 = "TEST_GUID3"
    private final String PLAN_GUID = "PLAN_GUID"



    private ServiceDefinitionConfig serviceDefinitionConfig
    private ServiceDefinitionInitializer serviceDefinitionInitializer
    private CFServiceRepository cfServiceRepository
    private PlanRepository planRepository
    private PlanMetadataRepository planMetadataRepository
    private ServiceInstanceRepository serviceInstanceRepository
    private ParameterRepository parameterRepository
    private ServiceDefinitionProcessor serviceDefinitionProcessor

    private List<ServiceDto> cfServiceList
    private HashMap<String, CFService> cfServiceHashMap = new HashMap<>()

    def setup() {
        cfServiceList = [new ServiceDto(guid: TEST_GUID)]
        cfServiceHashMap.put(TEST_GUID, new CFService(guid: TEST_GUID))
        cfServiceRepository = Mock(CFServiceRepository)
        planRepository = Mock(PlanRepository)
        planMetadataRepository = Mock(PlanMetadataRepository)
        serviceInstanceRepository = Mock(ServiceInstanceRepository)
        parameterRepository = Mock(ParameterRepository)
        serviceDefinitionConfig = new ServiceDefinitionConfig(serviceDefinitions: [new ServiceDto(guid: TEST_GUID)])
        serviceDefinitionProcessor = Mock(ServiceDefinitionProcessor)
        serviceDefinitionInitializer = new ServiceDefinitionInitializer(cfServiceRepository, planRepository, planMetadataRepository, parameterRepository, serviceInstanceRepository, serviceDefinitionConfig, serviceDefinitionProcessor)
    }

    def "Matching service definitions"() {
        when:
        serviceDefinitionInitializer.synchroniseServiceDefinitions(cfServiceList, cfServiceHashMap)

        then:
        noExceptionThrown()
    }

    def "Adding service definition from config"() {
        given:
        serviceDefinitionConfig.serviceDefinitions << new ServiceDto(name: TEST_GUID2)

        when:
        serviceDefinitionInitializer.init()

        then:
        noExceptionThrown()
    }

    def "Service definition missing from config"() {
        given:
        cfServiceHashMap.put(TEST_GUID3, new CFService(guid: TEST_GUID3))

        when:
        serviceDefinitionInitializer.synchroniseServiceDefinitions(cfServiceList, cfServiceHashMap)

        then:
        noExceptionThrown()
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

    def "Successfully delete plan"() {
        given:
        Plan plan = new Plan(guid: PLAN_GUID)
        CFService cfService = new CFService(guid: TEST_GUID)
        plan.service = cfService
        cfService.plans.add(plan)

        when:
        def deleted = serviceDefinitionInitializer.tryDeletePlan(plan)

        then:
        assert(deleted)
        noExceptionThrown()
    }
}
