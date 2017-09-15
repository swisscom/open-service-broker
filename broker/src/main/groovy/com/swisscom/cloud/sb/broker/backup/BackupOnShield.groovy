package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.backup.shield.ShieldClient
import com.swisscom.cloud.sb.broker.backup.shield.ShieldConfig
import com.swisscom.cloud.sb.broker.backup.shield.ShieldServiceConfig
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
trait BackupOnShield {
    private final String SERVICE_DEFINITION_PARAMETER_BACKUP_PREFIX = "BACKUP_"

    @Autowired
    ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    ShieldClient shieldClient

    @Autowired
    ShieldConfig shieldConfig

    abstract ShieldTarget buildShieldTarget(ServiceInstance serviceInstance)

    abstract String shieldAgentUrl(ServiceInstance serviceInstance)

    ShieldServiceConfig shieldServiceConfig(ServiceInstance serviceInstance) {
        shieldServiceConfigFromParameters(serviceInstance.plan.parameters)
    }

    ShieldServiceConfig shieldServiceConfigFromParameters(Set<Parameter> parameters) {
        def planParamtersForBackup = parameters.findAll {
            it.getName().startsWith(SERVICE_DEFINITION_PARAMETER_BACKUP_PREFIX)
        }
        String scheduleName = planParamtersForBackup.find {
            it.getName().equals("BACKUP_SCHEDULE_NAME")
        }?.getValue()
        String policyName = planParamtersForBackup.find {
            it.getName().equals("BACKUP_POLICY_NAME")
        }?.getValue()
        String storageName = planParamtersForBackup.find {
            it.getName().equals("BACKUP_STORAGE_NAME")
        }?.getValue()
        new ShieldServiceConfig(scheduleName: scheduleName, retentionName: policyName, storeName: storageName)
    }
}