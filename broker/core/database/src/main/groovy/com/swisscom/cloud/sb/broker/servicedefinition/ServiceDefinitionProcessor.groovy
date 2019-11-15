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

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.cfapi.dto.SchemasDto
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.metrics.PlanMetricService
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.repository.*
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.JsonSchemaHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class ServiceDefinitionProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDefinitionProcessor.class)

    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private PlanRepository planRepository
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private TagRepository tagRepository
    @Autowired
    private PlanMetadataRepository planMetadataRepository
    @Autowired
    private CFServiceMetaDataRepository cfServiceMetaDataRepository
    @Autowired
    private CFServicePermissionRepository servicePermissionRepository
    @Autowired
    private ParameterRepository parameterRepository

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    private List<PlanMetricService> planBasedMetricServices

    def createOrUpdateServiceDefinitionFromYaml(ServiceDto serviceDto) {
        CFService service = processServiceBasicDefiniton(serviceDto)
        processServiceTags(service, serviceDto)
        processServiceMetadata(service, serviceDto)
        processServicePermissions(service, serviceDto)
        processPlans(service, serviceDto)
    }

    private CFService processServiceBasicDefiniton(serviceJson) {
        CFService service = findOrCreateService(serviceJson)
        service.name = serviceJson.name
        service.description = serviceJson.description
        service.bindable = serviceJson.bindable
        service.active = serviceJson.active ?: true
        service.internalName = serviceJson.internalName
        service.serviceProviderClass = serviceJson.serviceProviderClass
        service.displayIndex = serviceJson.displayIndex
        service.asyncRequired = serviceJson.asyncRequired
        service.plan_updateable = serviceJson.plan_updateable
        service.instancesRetrievable = serviceJson.instancesRetrievable
        service.bindingsRetrievable = serviceJson.bindingsRetrievable
        // dashboard items
        if (serviceJson.dashboard_client) {
            service.dashboardClientId = serviceJson.dashboard_client.id
            service.dashboardClientSecret = serviceJson.dashboard_client.secret
            service.dashboardClientRedirectUri = serviceJson.dashboard_client.redirect_uri
        } else {
            service.dashboardClientId = null
            service.dashboardClientSecret = null
            service.dashboardClientRedirectUri = null
        }
        return cfServiceRepository.saveAndFlush(service)
    }

    private CFService findOrCreateService(serviceJson) {
        CFService service = cfServiceRepository.findByGuid(serviceJson.guid)
        if (!service) {
            service = new CFService()
            service.guid = serviceJson.guid
            return cfServiceRepository.saveAndFlush(service)
        }
        return service
    }

    private def processServiceTags(CFService service, serviceJson) {
        removeServiceTags(service)
        addNewServiceTagsFromJson(serviceJson, service)
    }

    private void addNewServiceTagsFromJson(serviceJson, CFService service) {
        serviceJson.tags.each {
            def tag = new Tag(tag: it)
            tagRepository.save(tag)
            service.tags.add(tag)
        }
        cfServiceRepository.save(service)
    }

    private void removeServiceTags(CFService service) {
        if (service.tags) {
            copyOf(service.tags).each {
                service.tags.remove(it)
                tagRepository.delete(it)
            }
            cfServiceRepository.saveAndFlush(service)
        }
    }

    private void processServiceMetadata(CFService service, serviceJson) {
        removeServiceMetadata(service)
        addNewServiceMetadataFromJson(serviceJson, service)
    }

    private void removeServiceMetadata(CFService service) {
        if (service.metadata) {
            copyOf(service.metadata).each {
                service.metadata.remove(it)
                cfServiceMetaDataRepository.delete(it)
            }
            cfServiceRepository.saveAndFlush(service)
        }
    }

    private def demapify(Object value) {
        if (value instanceof LinkedHashMap) {
            return ((LinkedHashMap) value).values().toArray()
        }

        return value;
    }

    private void addNewServiceMetadataFromJson(serviceJson, CFService service) {
        serviceJson.metadata.each {
            k, v ->
                def value = demapify(v)
                // Do not use ObjectMapper for String values because it will add quotes
                def serializedObject = (value.getClass() == String.class) ? value : objectMapper.writeValueAsString(
                        value)
                def serviceMetadata = new CFServiceMetadata(key: k,
                                                            value: serializedObject,
                                                            type: value.getClass().name)
                cfServiceMetaDataRepository.saveAndFlush(serviceMetadata)
                service.metadata.add(serviceMetadata)
                cfServiceRepository.saveAndFlush(service)
        }
    }

    private processServicePermissions(CFService service, serviceJson) {
        removeServicePermissions(service)
        addNewServicePermissionsFromJson(serviceJson, service)
    }

    private void removeServicePermissions(CFService service) {
        if (service.permissions) {
            copyOf(service.permissions).each {
                service.permissions.remove(it)
                servicePermissionRepository.delete(it)
            }
            cfServiceRepository.saveAndFlush(service)
        }
    }

    private void addNewServicePermissionsFromJson(serviceJson, CFService service) {
        serviceJson.requires.each {
            def permission = new CFServicePermission(permission: it)
            servicePermissionRepository.saveAndFlush(permission)
            service.permissions.add(permission)
        }
        cfServiceRepository.saveAndFlush(service)
    }

    private void processPlans(CFService service, serviceJson) {
        processExistingPlans(service, serviceJson)
        processNewPlansFromJson(serviceJson, service)
    }

    private void processExistingPlans(CFService service, serviceJson) {
        copyOf(service.plans).each {
            Plan plan ->
                removePlanIfNotContainedInJsonAndNotInUse(plan, service, serviceJson)
        }
    }

    private void removePlanIfNotContainedInJsonAndNotInUse(Plan plan, CFService service, serviceJson) {
        if (isPlanIncludedInJson(serviceJson, plan)) {
            return
        }

        if (!isPlanInUse(plan)) {
            LOGGER.warn("Plan:${plan.guid} will be removed(there are no service instances with this plan.")
            service.plans.remove(plan)
            cfServiceRepository.saveAndFlush(service)
            planRepository.delete(plan)
        } else {
            plan.active = false
            planRepository.saveAndFlush(plan)
        }
    }

    private static boolean isPlanIncludedInJson(serviceJson, Plan plan) {
        return serviceJson.plans.find {plan.guid == it.guid}
    }


    private boolean isPlanInUse(Plan plan) {
        return !serviceInstanceRepository.findByPlan(plan).isEmpty()
    }

    private Object processNewPlansFromJson(serviceJson, CFService service) {
        return serviceJson.plans.each {
            Object planJson ->
                Plan plan = processPlanBasicDefinition(service, planJson)
                processPlanParameters(plan, planJson)
                processPlanMetadata(plan, planJson)
                planBasedMetricServices.each { mS -> mS.bindMetricsPerPlan(plan) }
        }
    }

    private Plan processPlanBasicDefinition(CFService service, planJson) {
        Plan plan = createPlanIfDoesNotExist(planJson, service)
        plan.service = service
        plan.name = planJson.name
        plan.description = planJson.description
        plan.templateUniqueIdentifier = planJson.templateId
        plan.templateVersion = planJson.templateVersion
        plan.active = planJson.active ?: true
        plan.free = planJson.free
        plan.displayIndex = planJson.displayIndex
        plan.internalName = planJson.internalName
        plan.serviceProviderClass = planJson.serviceProviderClass
        plan.asyncRequired = planJson.asyncRequired
        plan.maxBackups = planJson.maxBackups

        def om = new ObjectMapper()
        if (planJson.schemas != null) {
            SchemasDto schemaDto = om.readValue(om.writeValueAsString(planJson.schemas), SchemasDto.class)

            validateJsonSchema(schemaDto.serviceInstanceSchema?.createMethodSchema?.configParametersSchema,
                               'ServiceInstanceCreate')
            validateJsonSchema(schemaDto.serviceInstanceSchema?.updateMethodSchema?.configParametersSchema,
                               'ServiceInstanceUpdate')
            validateJsonSchema(schemaDto.serviceBindingSchema?.createMethodSchema?.configParametersSchema,
                               'ServiceBindingCreate')

            plan.serviceInstanceCreateSchema =  JsonHelper.
                    toJsonString(schemaDto.serviceInstanceSchema?.createMethodSchema?.configParametersSchema)
            plan.serviceInstanceUpdateSchema = JsonHelper.
                    toJsonString(schemaDto.serviceInstanceSchema?.updateMethodSchema?.configParametersSchema)
            plan.serviceBindingCreateSchema = JsonHelper.
                    toJsonString(schemaDto.serviceBindingSchema?.createMethodSchema?.configParametersSchema)
        }

        return planRepository.saveAndFlush(plan)
    }

    private def validateJsonSchema(Object o, String schemaName) {
        def json = JsonHelper.toJsonString(o)
        if (json) {
            def validationMessages = JsonSchemaHelper.validateJson(json)
            if (!validationMessages.isEmpty()) {
                LOGGER.error("Invalid schema for ${schemaName}: " + JsonHelper.toJsonString(validationMessages))
                ErrorCode.INVALID_PLAN_SCHEMAS.throwNew()
            }
        }
    }

    private Plan createPlanIfDoesNotExist(planJson, CFService service) {
        Plan plan = planRepository.findByGuid(planJson.guid)
        if (!plan) {
            plan = new Plan(guid: planJson.guid)
            planRepository.saveAndFlush(plan)
            service.plans.add(plan)
            cfServiceRepository.saveAndFlush(service)
        }
        return plan
    }

    private void processPlanParameters(Plan plan, planJson) {
        removeExistingPlanParameters(plan)
        addPlanParametersFromJson(planJson, plan)
    }

    private void removeExistingPlanParameters(Plan plan) {
        if (plan.parameters) {
            copyOf(plan.parameters).each {
                plan.parameters.remove(it)
                planRepository.saveAndFlush(plan)
                parameterRepository.delete(it)
            }
        }
    }

    private void addPlanParametersFromJson(planJson, Plan plan) {
        def params = []
        if (planJson.containerParams) {
            params.addAll(planJson.containerParams)
        }
        if (planJson.parameters) {
            params.addAll(planJson.parameters)
        }

        params.each {
            def param ->
                Parameter parameter = new Parameter(template: param.template,
                                                    name: param.name,
                                                    value: param.value)
                parameterRepository.saveAndFlush(parameter)
                plan.parameters.add(parameter)
        }
        planRepository.saveAndFlush(plan)
    }

    private void processPlanMetadata(Plan plan, planJson) {
        removeExistingPlanMetadata(plan)
        addPlanMetadataFromJson(planJson, plan)
    }

    private void removeExistingPlanMetadata(Plan plan) {
        if (plan.metadata) {
            copyOf(plan.metadata).each {
                plan.metadata.remove(it)
                planRepository.saveAndFlush(plan)
                planMetadataRepository.delete(it)
            }
        }
    }

    private void addPlanMetadataFromJson(planJson, Plan plan) {
        planJson.metadata.each {
            k, v ->
                def value = demapify(v)
                // Do not use ObjectMapper for String values because it will add quotes
                def serializedObject = (value.getClass() == String.class) ? value : objectMapper.writeValueAsString(
                        value)
                def planMetadata = new PlanMetadata(key: k, value: serializedObject, type: value.getClass().name)
                planMetadataRepository.saveAndFlush(planMetadata)
                plan.metadata.add(planMetadata)
        }
        planRepository.saveAndFlush(plan)
    }


    private static HashSet copyOf(collection2copy) {
        new HashSet<>(collection2copy)
    }
}
