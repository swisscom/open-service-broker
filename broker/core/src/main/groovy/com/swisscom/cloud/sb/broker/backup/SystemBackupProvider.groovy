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

package com.swisscom.cloud.sb.broker.backup

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic

@CompileStatic
trait SystemBackupProvider extends BackupOnShield {

    String systemBackupJobName(String jobPrefix, String serviceInstanceId) {
        backupJobName(jobPrefix, serviceInstanceId)
    }

    String systemBackupTargetName(String targetPrefix, String serviceInstanceId) {
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

    void unregisterSystemBackupOnShield(String serviceInstanceId) {
        String jobName = systemBackupJobName(shieldConfig.jobPrefix, serviceInstanceId)
        String targetName = systemBackupTargetName(shieldConfig.targetPrefix, serviceInstanceId)
        shieldClient.unregisterSystemBackup(jobName, targetName)
    }
}
