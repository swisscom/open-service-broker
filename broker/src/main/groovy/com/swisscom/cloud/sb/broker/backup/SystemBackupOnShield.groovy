package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.backup.shield.ShieldClient
import com.swisscom.cloud.sb.broker.backup.shield.ShieldConfig
import com.swisscom.cloud.sb.broker.backup.shield.ShieldServiceConfig
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
trait SystemBackupOnShield {
    private final String SERVICE_DEFINITION_PARAMETER_BACKUP_PREFIX = "BACKUP_"

    @Autowired
    protected ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    private ShieldClient shieldClient

    @Autowired
    protected ShieldConfig shieldConfig

    abstract ShieldTarget createShieldTarget(ServiceInstance serviceInstance)

    abstract String systemBackupJobName(String jobPrefix, String serviceInstance)

    abstract String systemBackupTargetName(String targetPrefix, String serviceInstance)

    abstract String shieldAgent(ServiceInstance serviceInstance)

    Collection<ServiceDetail> configureSystemBackup(String serviceInstanceId) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(serviceInstanceId)
        def shieldServiceConfig = shieldServiceConfig(serviceInstance.plan.parameters)
        def shieldTarget = createShieldTarget(serviceInstance)
        def jobName = systemBackupJobName(shieldConfig.jobPrefix, serviceInstanceId)
        def targetName = systemBackupTargetName(shieldConfig.targetPrefix, serviceInstanceId)
        shieldClient.registerAndRunSystemBackup(jobName, targetName, shieldTarget, shieldServiceConfig, shieldAgent(serviceInstance))
    }

    def unregisterSystemBackupOnShield(String serviceInstanceId) {
        def jobName = systemBackupJobName(shieldConfig.jobPrefix, serviceInstanceId)
        def targetName = systemBackupTargetName(shieldConfig.targetPrefix, serviceInstanceId)
        shieldClient.unregisterSystemBackup(jobName, targetName)
    }

    private ShieldServiceConfig shieldServiceConfig(Set<Parameter> parameters) {
        def planParamtersForBackup = parameters.findAll {
            it.getName().startsWith(SERVICE_DEFINITION_PARAMETER_BACKUP_PREFIX)
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
}
