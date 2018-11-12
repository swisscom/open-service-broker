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
import com.swisscom.cloud.sb.broker.model.repository.*
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
    ServiceDefinitionInitializer(
            CFServiceRepository cfServiceRepository,
            PlanRepository planRepository,
            PlanMetadataRepository planMetadataRepository,
            ParameterRepository parameterRepository,
            ServiceInstanceRepository serviceInstanceRepository,
            ServiceDefinitionConfig serviceDefinitionConfig,
            ServiceDefinitionProcessor serviceDefinitionProcessor)
    {
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
        log.info("Start ServiceDefinition Synchronization (Is:${toBeDeleted.size()},Should:${services.size()})")
        services.each{ service ->
            log.info("Add or Update (name:${service.name}).")
            addOrUpdateServiceDefinitions(service)

            if(toBeDeleted.containsKey(service.guid)) {
                toBeDeleted.remove(service.guid)
            }
        }

        toBeDeleted.each{ key, service ->
            log.info("Delete/Disable Service (name:${service.name})")
            def canDeleteService = true

            service.plans.toList().each{ plan ->
                log.info("+ Delete/Disable Plan (serviceName:${service.name},name:${plan.name},id:${plan.id})")
                canDeleteService = canDeleteService & tryDeletePlan(plan)
            }

            def currentService = cfServiceRepository.getOne(service.id)
            if(canDeleteService) {
                log.info("DELETE Service (name:${currentService.name})")
                deleteServiceHibernateCacheSavely(currentService)
            } else {
                log.info("DISABLE Service (name:${currentService.name})")
                currentService.active = false
                cfServiceRepository.saveAndFlush(currentService)
            }
        }
    }

    void addOrUpdateServiceDefinitions(ServiceDto service) {
        serviceDefinitionProcessor.createOrUpdateServiceDefinitionFromYaml(service)
    }

    boolean tryDeletePlan(Plan plan) {
        if(serviceInstanceRepository.findByPlan(plan)) {
            if(plan.active) {
                log.info("+ DISABLE Plan (serviceName:${plan.service.name},name:${plan.name},id:${plan.id})")
                plan.active = false
                planRepository.saveAndFlush(plan)
            }
            return false
        } else {
            log.info("+ DELETE Plan (serviceName:${plan.service.name},name:${plan.name},id:${plan.id})")
            planRepository.delete(plan)
            planRepository.flush()

            def currentService = cfServiceRepository.getOne(plan.service.id)
            currentService.plans.remove(plan)
            cfServiceRepository.saveAndFlush(currentService)

            return true
        }
    }

    void deleteServiceHibernateCacheSavely(CFService service) {
        cfServiceRepository.delete(cfServiceRepository.findByGuid(service.guid))
        cfServiceRepository.flush()
    }
}
