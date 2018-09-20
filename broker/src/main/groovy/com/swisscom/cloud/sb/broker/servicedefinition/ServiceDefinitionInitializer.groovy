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
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import com.swisscom.cloud.sb.broker.model.repository.ParameterRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanMetadataRepository
import com.swisscom.cloud.sb.broker.model.repository.PlanRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@EnableConfigurationProperties
@Slf4j
class ServiceDefinitionInitializer {

    private CFServiceRepository cfServiceRepository

    private PlanRepository planRepository

    private PlanMetadataRepository planMetadataRepository

    private ParameterRepository parameterRepository

    private ServiceInstanceRepository serviceInstanceRepository

    private ServiceDefinitionConfig serviceDefinitionConfig

    private ServiceDefinitionProcessor serviceDefinitionProcessor

    @Autowired
    ServiceDefinitionInitializer(CFServiceRepository cfServiceRepository, PlanRepository planRepository, PlanMetadataRepository planMetadataRepository, ParameterRepository parameterRepository, ServiceInstanceRepository serviceInstanceRepository, ServiceDefinitionConfig serviceDefinitionConfig, ServiceDefinitionProcessor serviceDefinitionProcessor) {
        this.cfServiceRepository = cfServiceRepository
        this.planRepository = planRepository
        this.planMetadataRepository = planMetadataRepository
        this.parameterRepository = parameterRepository
        this.serviceInstanceRepository = serviceInstanceRepository
        this.serviceDefinitionConfig = serviceDefinitionConfig
        this.serviceDefinitionProcessor = serviceDefinitionProcessor
    }

    @PostConstruct
    void init() {
        List<ServiceDto> cfServiceListFromConfig = serviceDefinitionConfig.serviceDefinitions
        Map<String, CFService> cfServiceListFromDB = getServicesFromDB()

        synchroniseServiceDefinitions(cfServiceListFromConfig, cfServiceListFromDB)
    }

    HashMap<String, CFService> getServicesFromDB(){
        HashMap<String, CFService> cfServiceMap = new HashMap<String, CFService>()
        List<CFService> cfServiceList = cfServiceRepository.findAll()
        cfServiceList.each { cfService ->
            cfServiceMap.put(cfService.guid, cfService)
        }
        return cfServiceMap
    }

    void synchroniseServiceDefinitions(List<ServiceDto> services, HashMap<String, CFService> toBeDeleted) {
        services.each{ service ->
            addOrUpdateServiceDefinitions(service)
            if(toBeDeleted.containsKey(service.guid)) {
                toBeDeleted.remove(service.guid)
            }
        }

        toBeDeleted.each{ key, service ->
            def canDeleteService = true
            service.plans.toList().each{ plan ->
                canDeleteService = canDeleteService & tryDeletePlan(plan)
            }
            if(canDeleteService) {
                deleteServiceHibernateCacheSavely(service)
            } else {
                service.active = false
                cfServiceRepository.saveAndFlush(service)
            }
        }
    }

    void addOrUpdateServiceDefinitions(ServiceDto service) {
        serviceDefinitionProcessor.createOrUpdateServiceDefinitionFromYaml(service)
    }

    boolean tryDeletePlan(Plan plan) {
        if(serviceInstanceRepository.findByPlan(plan)) {
            if(plan.active) {
                plan.active = false
                planRepository.saveAndFlush(plan)
            }
            return false
        } else {
            plan.service.plans.remove(plan)
            cfServiceRepository.saveAndFlush(plan.service)
            planRepository.delete(plan)
            planRepository.flush()
            return true
        }
    }

    ArrayList<String> getListOfMissingServiceDefinitionGuids(List<String> configGuidList, List<String> guidList) {
        def listOfMissingServiceDefinitions = new ArrayList<String>()
        // TODO: do with hashmap rather than iterating over 2 lists
        guidList.each { guid ->
            if (!configGuidList.contains(guid)) {
                listOfMissingServiceDefinitions.add(guid)
            }
        }
        return listOfMissingServiceDefinitions
    }

    ArrayList<CFService> getMissingServices(List<String> missingServiceGuids) {
        def missingServices = new ArrayList<CFService>()
        missingServiceGuids.each { guid ->
            missingServices.add(cfServiceRepository.findByGuid(guid))
        }
        return missingServices
    }

    void deleteServiceDefinitionOrFlagAsInactive(List<CFService> missingServices) {
        missingServices.plans.collectMany{service ->
            service.each { plan ->
                if(serviceInstanceRepository.findByPlan(plan)) {
                    plan.active = false
                    planRepository.saveAndFlush(plan)
                } else {
                    plan.service.plans.remove(plan)
                    cfServiceRepository.saveAndFlush(plan.service)
                }
            }
        }

    void deleteServiceHibernateCacheSavely(CFService service) {
        cfServiceRepository.delete(cfServiceRepository.findByGuid(service.guid))
        cfServiceRepository.flush()
    }
}
