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

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.servicedefinition.converter.PlanDtoConverter
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ParameterDto
import com.swisscom.cloud.sb.broker.servicedefinition.dto.PlanDto
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback

@Rollback(true)
class ServiceDefinitionInitializerSpec extends BaseTransactionalSpecification {

    @Autowired
    private CFServiceRepository cfServiceRepository

    @Autowired
    private PlanRepository planRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private PlanDtoConverter planDtoConverter

    @Autowired
    private ServiceDefinitionInitializer serviceDefinitionInitializer

    @Autowired
    private ServiceDefinitionConfig serviceDefinitionConfig

    private List<CFService> cfServiceList

    def setup() {
        cfServiceList = cfServiceRepository.findAll()

        List<ServiceDto> serviceDtoList = new ArrayList<>()
        cfServiceList.each {
            def metadataMap = [key: it.metadata[0].key, value: it.metadata[0].value, type: it.metadata[0].type, service: it.metadata[0].service]
            serviceDtoList << new ServiceDto(guid: it.guid, name: it.name, internalName: it.internalName,
                    displayIndex: it.displayIndex, asyncRequired: it.asyncRequired, id: it.id,
                    description: it.description, bindable: it.bindable, tags: new ArrayList<>(it.tags),
                    plans: planDtoConverter.convertAll(it.plans), metadata: metadataMap)
        }

        serviceDefinitionConfig.serviceDefinitions = serviceDtoList
    }

    def "Match service definitions"() {
        when:
        serviceDefinitionInitializer.init()

        then:
        cfServiceList == cfServiceRepository.findAll()

        and:
        noExceptionThrown()
    }

    def "Add service definition"() {
        given:
        serviceDefinitionConfig.serviceDefinitions <<
                new ServiceDto(guid: "guid", name: "name", internalName: "internalName",
                        displayIndex: 1, asyncRequired: false, id: "id", description: "description", bindable: true, tags: ["tag"],
                        plans: [new PlanDto(guid: "guid", name: "planName", templateId: "templateId", templateVersion: "templateVersion", internalName: "internalName", displayIndex: 1,
                                asyncRequired: false, maxBackups: 0, parameters: [new ParameterDto(template: "template", name: "name",
                                value: "value")])], metadata: [key: "key", value: "value", type: "type", service: new CFService(guid: "guid")])

        when:
        serviceDefinitionInitializer.init()

        then:
        cfServiceList != cfServiceRepository.findAll()

        and:
        noExceptionThrown()
    }

    def "Update service definition"() {
        given:
        serviceDefinitionConfig.serviceDefinitions << new ServiceDto(guid: "guid", name: "name", internalName: "internalName",
                displayIndex: 1, asyncRequired: false, id: "id", description: "description", bindable: true, tags: ["tag"],
                plans: [new PlanDto(guid: "guid", name: "planName", templateId: "templateId", templateVersion: "templateVersion", internalName: "internalName", displayIndex: 1,
                        asyncRequired: false, maxBackups: 0, parameters: [new ParameterDto(template: "template", name: "name",
                        value: "value")])], metadata: [key: "key", value: "value", type: "type", service: new CFService(guid: "guid")])

        and:
        serviceDefinitionInitializer.init()

        when:
        serviceDefinitionConfig.serviceDefinitions << new ServiceDto(guid: "guid", name: "name", internalName: "internalName",
                displayIndex: 1, asyncRequired: false, id: "id", description: "description", bindable: false, tags: ["tag"],
                plans: [new PlanDto(guid: "guid", name: "plaName", templateId: "templateId", templateVersion: "templateVersion", internalName: "internalName", displayIndex: 1,
                        asyncRequired: false, maxBackups: 0, parameters: [new ParameterDto(template: "template", name: "name",
                        value: "value")])], metadata: [key: "key", value: "value", type: "type", service: new CFService(guid: "guid")])

        and:
        serviceDefinitionInitializer.init()

        then:
        def cfService = cfServiceRepository.findByGuid("guid")
        assert (!cfService.bindable)

        and:
        noExceptionThrown()
    }

    def "Service definition from DB that is missing in config and has no service instances is deleted"() {
        given:
        def serviceGuid = "serviceWithoutInstance"
        CFService cfService = new CFService(guid: serviceGuid)
        cfServiceRepository.saveAndFlush(cfService)

        and:
        def planGuid = "planWithoutInstance"
        Plan plan = new Plan(guid: planGuid, service: cfService)
        planRepository.saveAndFlush(plan)

        and:
        cfService.plans.add(plan)
        cfServiceRepository.saveAndFlush(cfService)

        and:
        def service = cfServiceRepository.findByGuid(serviceGuid)
        assert (service)

        when:
        serviceDefinitionInitializer.init()

        then:
        assert (!cfServiceRepository.findByGuid(serviceGuid))
        assert (!planRepository.findByGuid(planGuid))

        and:
        noExceptionThrown()
    }

    def "service definition from DB that is missing from config that has service instance cannot be deleted but is marked as inactive"() {
        given:
        def serviceGuid = "serviceWithInstance"
        CFService cfService = new CFService(guid: serviceGuid)
        cfServiceRepository.saveAndFlush(cfService)

        and:
        def planGuid = "planWithInstance"
        Plan plan = new Plan(guid: planGuid, service: cfService)
        planRepository.saveAndFlush(plan)

        and:
        cfService.plans.add(plan)
        cfServiceRepository.saveAndFlush(cfService)

        and:
        ServiceInstance serviceInstance = new ServiceInstance(guid: "testServiceInstance", plan: plan)
        serviceInstanceRepository.saveAndFlush(serviceInstance)

        when:
        serviceDefinitionInitializer.init()

        then:
        assert(cfServiceRepository.findByGuid(serviceGuid))
        assert(planRepository.findByGuid(planGuid))
        assert (!cfServiceRepository.findByGuid(serviceGuid).active)
        assert (!planRepository.findByGuid(planGuid).active)

        and:
        noExceptionThrown()

        cleanup:
        serviceInstanceRepository.delete(serviceInstance)
        planRepository.delete(plan)
        cfServiceRepository.delete(cfService)
    }

    def "unused plans are deleted from service definition"() {
        given:
        def serviceGuid = "serviceWithInstance"
        CFService cfService = new CFService(guid: serviceGuid)
        cfServiceRepository.save(cfService)

        and:
        def plan1Guid = "planWithInstance"
        Plan plan1 = new Plan(guid: plan1Guid, service: cfService)
        planRepository.save(plan1)

        and:
        def plan2Guid = "planWithoutInstance"
        Plan plan2 = new Plan(guid: plan2Guid, service: cfService)
        planRepository.saveAndFlush(plan2)

        and:
        cfService.plans.add(plan1)
        cfService.plans.add(plan2)
        cfServiceRepository.saveAndFlush(cfService)

        and:
        ServiceInstance serviceInstance = new ServiceInstance(guid: "testServiceInstance2", plan: plan1)
        serviceInstanceRepository.saveAndFlush(serviceInstance)

        when:
        serviceDefinitionInitializer.init()

        then:
        assert (!cfServiceRepository.findByGuid(serviceGuid).active)
        assert (!planRepository.findByGuid(plan1Guid).active)
        assert (!planRepository.findByGuid(plan2Guid))

        and:
        noExceptionThrown()

        cleanup:
        serviceInstanceRepository.delete(serviceInstance)
        planRepository.delete(plan1)
        cfServiceRepository.delete(cfService)
    }
}
