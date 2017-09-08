package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.backup.shield.*
import com.swisscom.cloud.sb.broker.backup.shield.dto.*
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
trait SystemBackupOnShield {
    private final String PARAMETER_BACKUP_PREFIX = "BACKUP_"

    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    private ShieldRestClientFactory shieldRestClientFactory

    @Autowired
    private RestTemplateFactory restTemplateFactory

    @Autowired
    protected ShieldConfig shieldConfig

    abstract ShieldTarget createShieldTarget(ServiceInstance serviceInstance)

    abstract String jobName(String jobPrefix, String serviceInstance)

    abstract String targetName(String targetPrefix, String serviceInstance)

    abstract String shieldAgent(ServiceInstance serviceInstance)

    Collection<ServiceDetail> registerAndRunSystemBackupOnShield(String serviceInstanceGuid) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(serviceInstanceGuid)
        def shieldServiceConfig = shieldServiceConfig(serviceInstance.plan.parameters)

        def shieldTarget = createShieldTarget(serviceInstance)
        String targetUuid = createOrUpdateTarget(serviceInstance, shieldTarget)
        String jobUuid = registerJob(serviceInstance, shieldServiceConfig, targetUuid)

        buildClient(serviceInstance).runJob(jobUuid)

        [ServiceDetail.from(ServiceDetailKey.SHIELD_JOB_UUID, jobUuid),
         ServiceDetail.from(ServiceDetailKey.SHIELD_TARGET_UUID, targetUuid)]
    }

    def unregisterSystemBackupOnShield(String serviceInstanceId) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(serviceInstanceId)
        deleteJobIfExisting(serviceInstance)
        deleteTargetIfExisting(serviceInstance)
    }

    private ShieldServiceConfig shieldServiceConfig(Set<Parameter> parameters) {
        def planParamtersForBackup = parameters.findAll {
            it.getName().startsWith(PARAMETER_BACKUP_PREFIX)
        }
        String scheduleName = (planParamtersForBackup.find {
            it.getName().equals("BACKUP_SCHEDULE_NAME")
        } as Parameter).getValue()
        String policyName = (planParamtersForBackup.find {
            it.getName().equals("BACKUP_POLICY_NAME")
        } as Parameter).getValue()
        String storageName = (planParamtersForBackup.find {
            it.getName().equals("BACKUP_STORAGE_NAME")
        } as Parameter).getValue()
        new ShieldServiceConfig(scheduleName: scheduleName, retentionName: policyName, storeName: storageName)
    }

    private String registerJob(ServiceInstance serviceInstance, ShieldServiceConfig shieldServiceConfig, String targetUuid) {
        ShieldRestClient shieldRestClient = buildClient(serviceInstance)
        StoreDto store = shieldRestClient.getStoreByName(shieldServiceConfig.storeName)
        if (store == null) {
            throw new RuntimeException("Store ${shieldServiceConfig.storeName} that is configured does not exist on shield")
        }
        RetentionDto retention = shieldRestClient.getRetentionByName(shieldServiceConfig.retentionName)
        if (retention == null) {
            throw new RuntimeException("Retention ${shieldServiceConfig.retentionName} that is configured does not exist on shield")
        }
        ScheduleDto schedule = shieldRestClient.getScheduleByName(shieldServiceConfig.scheduleName)
        if (schedule == null) {
            throw new RuntimeException("Schedule ${shieldServiceConfig.scheduleName} that is configured does not exist on shield")
        }
        createOrUpdateJob(serviceInstance, targetUuid, store.uuid, retention.uuid, schedule.uuid)
    }

    private String createOrUpdateTarget(ServiceInstance serviceInstance, ShieldTarget target) {
        String targetName = targetName(shieldConfig.targetPrefix, serviceInstance.guid)
        TargetDto targetOnShield = buildClient(serviceInstance).getTargetByName(targetName)
        targetOnShield == null ? buildClient(serviceInstance).createTarget(targetName, target) : buildClient(serviceInstance).updateTarget(targetOnShield, target)
    }

    private String createOrUpdateJob(ServiceInstance serviceInstance,
                                     String targetUuid,
                                     String storeUuid,
                                     String retentionUuid,
                                     String scheduleUuid,
                                     boolean paused = false) {
        String jobName = jobName(shieldConfig.jobPrefix, serviceInstance.guid)
        JobDto jobOnShield = buildClient(serviceInstance).getJobByName(jobName)
        jobOnShield == null ?
                buildClient(serviceInstance).createJob(jobName, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused) :
                buildClient(serviceInstance).updateJob(jobOnShield, targetUuid, storeUuid, retentionUuid, scheduleUuid, paused)
    }

    private deleteJobIfExisting(ServiceInstance serviceInstance) {
        String jobName = jobName(shieldConfig.jobPrefix, serviceInstance.guid)
        JobDto job = buildClient(serviceInstance).getJobByName(jobName)
        if (job != null) {
            buildClient(serviceInstance).deleteJob(job.uuid)
        }
    }

    private deleteTargetIfExisting(ServiceInstance serviceInstance) {
        String targetName = targetName(shieldConfig.targetPrefix, serviceInstance.guid)
        TargetDto target = buildClient(serviceInstance).getTargetByName(targetName)
        if (target != null) {
            buildClient(serviceInstance).deleteTarget(target.uuid)
        }
    }

    private ShieldRestClient buildClient(ServiceInstance serviceInstance) {
        shieldRestClientFactory.build(restTemplateFactory.buildWithSSLValidationDisabled(), shieldConfig.baseUrl, shieldConfig.apiKey, shieldAgent(serviceInstance))
    }
}
