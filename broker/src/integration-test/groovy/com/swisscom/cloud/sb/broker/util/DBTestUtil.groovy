package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.model.repository.*
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DBTestUtil {

    public final String SERVICE_GUID = "service1-guid"
    public final String SERVICE_NAME = "service1-name"
    public final String SERVICE_DESCRIPTION = "service1-name"
    public final Boolean SERVICE_BINDABLE = true
    public final String SERVICE_DISPLAY_NAME = "Service1Name"
    public final String SERVICE_VERSION = "v1.0.3"


    public final String PLAN1_GUID = "plan1-guid"
    public final String PLAN1_NAME = "small"
    public final Boolean PLAN1_FREE = true
    public final String PLAN1_DESCRIPTION = "plan1 description"
    public final String PLAN1_DISPLAY_NAME = "Small Display Name"


    public final String PLAN2_GUID = "plan2-guid"
    public final String PLAN2_NAME = "large"
    public final Boolean PLAN2_FREE = false
    public final String PLAN2_DESCRIPTION = "plan2 description"
    public final String PLAN2_DISPLAY_NAME = "Large Display Name"

    public final String TEMPLATE1_GUID = "template1-guid"
    public final String TEMPLATE2_GUID = "template2-guid"

    public final String METADATA_KEY_DISPLAY_NAME = "displayName"
    public final String METADATA_KEY_VERSION = "version"


    @Autowired
    private CFServiceRepository cfServiceRepository
    @Autowired
    private PlanRepository planRepository
    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository
    @Autowired
    private ServiceBindingRepository serviceBindingRepository
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
    private LastOperationRepository lastOperationRepository
    @Autowired
    private BackupRepository backupRepository
    @Autowired
    private RestoreRepository restoreRepository
    @Autowired
    private ServiceDetailRepository serviceDetailRepository

    def createService() {
        return createService(SERVICE_GUID, SERVICE_NAME, SERVICE_DESCRIPTION, SERVICE_BINDABLE, SERVICE_DISPLAY_NAME, SERVICE_VERSION)
    }

    def createPlan1() {
        return createPlan(PLAN1_NAME, PLAN1_GUID, PLAN1_FREE, PLAN1_DESCRIPTION, TEMPLATE1_GUID)
    }

    def createPlan2() {
        return createPlan(PLAN2_NAME, PLAN2_GUID, PLAN2_FREE, PLAN2_DESCRIPTION, TEMPLATE2_GUID)
    }


    def createServiceWith2Plans() {
        def service = createService()
        def plan1 = createPlan1()
        def plan2 = createPlan2()
        service.plans.add(plan1)
        service.plans.add(plan2)
        cfServiceRepository.save(service)
    }

    def addTagsToService(CFService service, List<String> tags) {
        tags.each {
            def tag = new Tag(tag: it)
            tag = tagRepository.save(tag)
            service.tags.add(tag)
        }
        cfServiceRepository.save(service)
    }


    def
    createService(String guid, String name, String description, Boolean bindable, String displayName, String version) {
        def service = new CFService(guid: guid, name: name, description: description, bindable: bindable)
        service.metadata.add(new CFServiceMetadata(key: METADATA_KEY_DISPLAY_NAME, value: displayName))
        service.metadata.add(new CFServiceMetadata(key: METADATA_KEY_VERSION, value: version))
        cfServiceRepository.save(service)
    }

    def createServiceInstace(CFService service, String guid, List<ServiceDetail> details = null) {
        def serviceInstance = serviceInstanceRepository.save(new ServiceInstance(guid: guid, plan: service.plans.first(), completed: true))
        details?.each {
            def detail = serviceDetailRepository.save(ServiceDetail.from(ServiceDetailKey.PORT, '1000'))
            serviceInstance.details.add(detail)
        }
        serviceInstanceRepository.save(serviceInstance)

        return serviceInstance
    }

    def createServiceBinding(CFService service, ServiceInstance serviceInstance, String bindingGuid, String credentials) {
        def binding = new ServiceBinding()
        binding.serviceInstance = serviceInstance
        binding.guid = bindingGuid
        binding.credentials = credentials
        serviceBindingRepository.save(binding)
        serviceInstance.bindings.add(binding)
        cfServiceRepository.save(service)
        return binding
    }

    def createPlan(String name, String guid, Boolean free, String description, String templateId) {
        return planRepository.save(new Plan(name: name, guid: guid, free: free, description: description, templateUniqueIdentifier: templateId, maxBackups: 0))
    }

    def createPlanExtra(String key, String value) {
        return planMetadataRepository.save(new PlanMetadata(key: key, value: value))
    }

    def
    createLastOperation(String id, LastOperation.Status status = LastOperation.Status.IN_PROGRESS, Date date = new Date()) {
        return lastOperationRepository.save(new LastOperation(guid: id, dateCreation: date, operation: LastOperation.Operation.PROVISION, status: status, description: 'description'))
    }

    def createBackup(ServiceInstance serviceInstance, Backup.Operation operation, Backup.Status status) {
        return backupRepository.save(new Backup(serviceInstanceGuid: serviceInstance.guid, service: serviceInstance.plan.service, plan: serviceInstance.plan, guid: StringGenerator.randomUuid(), operation: operation, status: status, dateRequested: new Date()))
    }

    def createRestore(Backup backup, Backup.Status status) {
        Restore restore = restoreRepository.save(new Restore(backup: backup, guid: StringGenerator.randomUuid(), dateRequested: new Date(), status: status))
        backup.restores.add(restore)
        backupRepository.save(backup)
    }
}
