package com.swisscom.cloud.sb.broker.servicedefinition

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.swisscom.cloud.sb.broker.backup.BackupRestoreProvider
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.*
import com.swisscom.cloud.sb.broker.servicedefinition.converter.ServiceDtoConverter
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
@Slf4j
class ServiceDefinitionProcessor {
    @Autowired
    ServiceProviderLookup serviceProviderLookup

    @Autowired
    ServiceDtoConverter serviceDtoConverter

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

    def createOrUpdateServiceDefinition(String content) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content),"Service Definition can't be empty")

        def serviceJson = new JsonSlurper().parseText(content)
        CFService service = processServiceBasicDefiniton(serviceJson)
        processServiceTags(service, serviceJson)
        processServiceMetadata(service, serviceJson)
        processServicePermissions(service, serviceJson)
        processPlans(service, serviceJson)
    }

    def createOrUpdateServiceDefinitionFromYaml(ServiceDto serviceDto) {
        CFService service = processServiceBasicDefiniton(serviceDto)
        processServiceTags(service, serviceDto)
        processServiceMetadata(service, serviceDto)
        processServicePermissions(service, serviceDto)
        processPlans(service, serviceDto)
    }
  
    def deleteServiceDefinition(String id) {
        CFService service = cfServiceRepository.findByGuid(id)
        if (!service) {
            ErrorCode.SERVICE_NOT_FOUND.throwNew()
        }

        def serviceInstances = serviceInstanceRepository.findByPlanIdIn(service.plans.collect { it.id })
        if (serviceInstances && serviceInstances.size() > 0) {
            ErrorCode.SERVICE_IN_USE.throwNew()
        }

        cfServiceRepository.delete(service)
    }

    ServiceDto getServiceDefinition(String id) {
        def serviceDto = serviceDtoConverter.convert(cfServiceRepository.findByGuid(id))
        serviceDto.plans = serviceDto.plans.sort { it.displayIndex }
        return serviceDto
    }

    private CFService processServiceBasicDefiniton(serviceJson) {
        CFService service = findOrCreateService(serviceJson)
        service.name = serviceJson.name
        service.description = serviceJson.description
        service.bindable = serviceJson.bindable
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
        return cfServiceRepository.save(service)
    }

    private CFService findOrCreateService(serviceJson) {
        CFService service = cfServiceRepository.findByGuid(serviceJson.guid)
        if (!service) {
            service = new CFService()
            service.guid = serviceJson.guid
            return cfServiceRepository.save(service)
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
            cfServiceRepository.save(service)
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
            cfServiceRepository.save(service)
        }
    }

    private void addNewServiceMetadataFromJson(serviceJson, CFService service) {
        serviceJson.metadata.each {
            k, v ->
                def serviceMetadata = new CFServiceMetadata(key: k, value: v, type: v.class.simpleName)
                cfServiceMetaDataRepository.save(serviceMetadata)
                service.metadata.add(serviceMetadata)
                cfServiceRepository.save(service)
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
            cfServiceRepository.save(service)
        }
    }

    private void addNewServicePermissionsFromJson(serviceJson, CFService service) {
        serviceJson.requires.each {
            def permission = new CFServicePermission(permission: it)
            servicePermissionRepository.save(permission)
            service.permissions.add(permission)
        }
        cfServiceRepository.save(service)
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
            log.warn("Plan:${plan.guid} will be removed(there are no service instances with this plan.")
            service.plans.remove(plan)
            cfServiceRepository.save(service)
            planRepository.delete(plan)
        } else {
            ErrorCode.PLAN_IN_USE.throwNew()
        }
    }

    protected boolean isPlanIncludedInJson(serviceJson, Plan plan) {
        return serviceJson.plans.find { plan.guid == it.guid }
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
        }
    }

    private Plan processPlanBasicDefinition(CFService service, planJson) {
        Plan plan = createPlanIfDoesNotExist(planJson, service)
        plan.name = planJson.name
        plan.description = planJson.description
        plan.templateUniqueIdentifier = planJson.templateId
        plan.templateVersion = planJson.templateVersion
        plan.free = planJson.free
        plan.displayIndex = planJson.displayIndex
        plan.internalName = planJson.internalName
        plan.serviceProviderClass = planJson.serviceProviderClass
        plan.asyncRequired = planJson.asyncRequired
        plan.maxBackups = planJson.maxBackups
        checkBackupSanity(service, plan)
        return planRepository.save(plan)
    }

    private def checkBackupSanity(CFService cfService, Plan plan) {
        if (plan.maxBackups <= 0) {
            return
        }
        def provider = serviceProviderLookup.findServiceProvider(cfService, plan)
        if (!(provider instanceof BackupRestoreProvider)) {
            throw new RuntimeException("Not allowed to set up maxBackups:${plan.maxBackups} for a service provider that does not support backup/restore")
        }
    }

    private Plan createPlanIfDoesNotExist(planJson, CFService service) {
        Plan plan = planRepository.findByGuid(planJson.guid)
        if (!plan) {
            plan = new Plan(guid: planJson.guid)
            planRepository.save(plan)
            service.plans.add(plan)
            cfServiceRepository.save(service)
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
                planRepository.save(plan)
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
                parameterRepository.save(parameter)
                plan.parameters.add(parameter)
        }
        planRepository.save(plan)
    }

    def processPlanMetadata(Plan plan, planJson) {
        removeExistingPlanMetadata(plan)
        addPlanMetadataFromJson(planJson, plan)
    }

    private void removeExistingPlanMetadata(Plan plan) {
        if (plan.metadata) {
            copyOf(plan.metadata).each {
                plan.metadata.remove(it)
                planRepository.save(plan)
                planMetadataRepository.delete(it)
            }
        }
    }

    private void addPlanMetadataFromJson(planJson, Plan plan) {
        planJson.metadata.each {
            k, v ->
                def planMetadata = new PlanMetadata(key: k, value: v, type: v.class.simpleName)
                planMetadataRepository.save(planMetadata)
                plan.metadata.add(planMetadata)
        }
        planRepository.save(plan)
    }


    static HashSet copyOf(collection2copy) {
        new HashSet<>(collection2copy)
    }
}
