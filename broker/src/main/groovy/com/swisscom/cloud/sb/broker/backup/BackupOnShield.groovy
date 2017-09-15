package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.backup.shield.ShieldClient
import com.swisscom.cloud.sb.broker.backup.shield.ShieldConfig
import com.swisscom.cloud.sb.broker.backup.shield.BackupParameter
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
trait BackupOnShield {
    private final String PLAN_PARAMETER_BACKUP_PREFIX = "BACKUP_"

    @Autowired
    ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    ShieldClient shieldClient

    @Autowired
    ShieldConfig shieldConfig

    abstract ShieldTarget buildShieldTarget(ServiceInstance serviceInstance)

    abstract String shieldAgentUrl(ServiceInstance serviceInstance)

    def backupJobName(String jobPrefix, String serviceInstanceId) {
        "${jobPrefix}${serviceInstanceId}"
    }

    def backupTargetName(String targetPrefix, String serviceInstanceId) {
        "${targetPrefix}${serviceInstanceId}"
    }

    BackupParameter getBackupParameter(ServiceInstance serviceInstance) {
        getBackupParameter(serviceInstance.plan.parameters)
    }

    BackupParameter getBackupParameter(Set<Parameter> parameters) {
        def planParamtersForBackup = parameters.findAll {
            it.getName().startsWith(PLAN_PARAMETER_BACKUP_PREFIX)
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
        new BackupParameter(scheduleName: scheduleName, retentionName: policyName, storeName: storageName)
    }
}