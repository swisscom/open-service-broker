package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic

@CompileStatic
trait SystemBackupProvider extends BackupOnShield {

    def systemBackupJobName(String jobPrefix, String serviceInstanceId) {
        backupJobName(jobPrefix, serviceInstanceId)
    }

    def systemBackupTargetName(String targetPrefix, String serviceInstanceId) {
        backupTargetName(targetPrefix, serviceInstanceId)
    }

    Collection<ServiceDetail> configureSystemBackup(String serviceInstanceId) {
        ServiceInstance serviceInstance = provisioningPersistenceService.getServiceInstance(serviceInstanceId)
        def shieldServiceConfig = getBackupParameter(serviceInstance)
        def shieldTarget = buildShieldTarget(serviceInstance)
        String jobName = systemBackupJobName(shieldConfig.jobPrefix, serviceInstanceId)
        String targetName = systemBackupTargetName(shieldConfig.targetPrefix, serviceInstanceId)
        shieldClient.registerAndRunSystemBackup(jobName, targetName, shieldTarget, shieldServiceConfig, shieldAgentUrl(serviceInstance))
    }

    def unregisterSystemBackupOnShield(String serviceInstanceId) {
        String jobName = systemBackupJobName(shieldConfig.jobPrefix, serviceInstanceId)
        String targetName = systemBackupTargetName(shieldConfig.targetPrefix, serviceInstanceId)
        shieldClient.unregisterSystemBackup(jobName, targetName)
    }
}