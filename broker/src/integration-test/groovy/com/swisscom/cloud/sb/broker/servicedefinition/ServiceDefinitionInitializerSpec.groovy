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
import com.swisscom.cloud.sb.broker.model.repository.CFServiceMetaDataRepository
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.ParameterRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanMetadataRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.TagRepository
import com.swisscom.cloud.sb.broker.servicedefinition.converter.PlanDtoConverter
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ParameterDto
import com.swisscom.cloud.sb.broker.servicedefinition.dto.PlanDto
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import org.springframework.beans.factory.annotation.Autowired

class ServiceDefinitionInitializerSpec extends BaseTransactionalSpecification {

    @Autowired
    private CFServiceRepository cfServiceRepository

    @Autowired
    private PlanDtoConverter planDtoConverter

    @Autowired
    private CFServiceMetaDataRepository cfServiceMetaDataRepository

    @Autowired
    private PlanRepository planRepository

    @Autowired
    private ParameterRepository parameterRepository

    @Autowired
    private PlanMetadataRepository planMetadataRepository

    @Autowired
    private TagRepository tagRepository

    private ServiceDefinitionProcessor serviceDefinitionProcessor

    private ServiceDefinitionInitializer serviceDefinitionInitializer

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

        serviceDefinitionConfig = new ServiceDefinitionConfig(serviceDefinitions: serviceDtoList)
        serviceDefinitionProcessor = new ServiceDefinitionProcessor(cfServiceRepository: cfServiceRepository,
                cfServiceMetaDataRepository: cfServiceMetaDataRepository, planRepository: planRepository,
                parameterRepository: parameterRepository, planMetadataRepository: planMetadataRepository,
                tagRepository: tagRepository)
        serviceDefinitionInitializer = new ServiceDefinitionInitializer(cfServiceRepository, serviceDefinitionConfig, serviceDefinitionProcessor)
    }

    def "Match service definitions"() {
        when:
        serviceDefinitionInitializer.init()

        then:
        cfServiceList == cfServiceRepository.findAll()
    }

    def "Add service definition"() {
        given:
        serviceDefinitionConfig.serviceDefinitions << new ServiceDto(guid: "guid", name: "name", internalName: "internalName",
                displayIndex: 1, asyncRequired: false, id: "id", description: "description", bindable: true, tags: ["tag"],
                plans: [new PlanDto(guid: "guid", templateId: "templateId", templateVersion: "templateVersion", internalName: "internalName", displayIndex: 1,
                asyncRequired: false, maxBackups: 0, parameters: [new ParameterDto(template: "template", name: "name",
                        value: "value")])], metadata: [key: "key", value: "value", type: "type", service: new CFService(guid: "guid")])

        when:
        serviceDefinitionInitializer.init()

        then:
        cfServiceList != cfServiceRepository.findAll()
    }
}
