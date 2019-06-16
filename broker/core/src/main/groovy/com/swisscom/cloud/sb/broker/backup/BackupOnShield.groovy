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

import com.swisscom.cloud.sb.broker.backup.shield.BackupParameter
import com.swisscom.cloud.sb.broker.backup.shield.ShieldClient
import com.swisscom.cloud.sb.broker.backup.shield.ShieldConfig
import com.swisscom.cloud.sb.broker.backup.shield.ShieldTarget
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired

import static com.swisscom.cloud.sb.broker.backup.shield.BackupParameter.backupParameter

@CompileStatic
trait BackupOnShield extends ExtensionProvider {
    private static final String PLAN_PARAMETER_BACKUP_PREFIX = "BACKUP_"
    private static final String POLICY_NAME = "BACKUP_POLICY_NAME"
    private static final String STORAGE_NAME = "BACKUP_STORAGE_NAME"
    private static final String SCHEDULE_NAME = "BACKUP_SCHEDULE_NAME"
    private static final String SCHEDULE = "BACKUP_SCHEDULE"

    @Autowired
    ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    ShieldClient shieldClient

    @Autowired
    ShieldConfig shieldConfig

    abstract ShieldTarget buildShieldTarget(ServiceInstance serviceInstance)

    abstract String shieldAgentUrl(ServiceInstance serviceInstance)

    String backupJobName(String jobPrefix, String serviceInstanceId) {
        "${jobPrefix}${serviceInstanceId}"
    }

    String backupTargetName(String targetPrefix, String serviceInstanceId) {
        "${targetPrefix}${serviceInstanceId}"
    }

    BackupParameter getBackupParameter(ServiceInstance serviceInstance) {
        getBackupParameterFromParameters(serviceInstance.getPlan().getParameters().toSet())
    }

    private BackupParameter getBackupParameterFromParameters(Set<Parameter> parameters) {
        Map<String, String> backupParameters = parameters.findAll {p ->
            p.getName().startsWith(PLAN_PARAMETER_BACKUP_PREFIX)
        }.collectEntries {[it.getName(), it.getValue()]}

        [POLICY_NAME, STORAGE_NAME].each {key ->
            if (!backupParameters.containsKey(key)) {
                throw new IllegalArgumentException("Backup parameters must contain '${key}'")
            }
        }

        BackupParameter.Builder backupParameterBuilder = backupParameter()

        backupParameterBuilder.retentionName(backupParameters.get(POLICY_NAME))
        backupParameterBuilder.storeName(backupParameters.get(STORAGE_NAME))

        // Either BACKUP_SCHEDULE or BACKUP_SCHEDULE_NAME must be set
        if (backupParameters.containsKey(SCHEDULE)) {
            backupParameterBuilder.schedule(backupParameters.get(SCHEDULE))
        } else {
            if (!backupParameters.containsKey(SCHEDULE_NAME)) {
                throw new IllegalArgumentException("Backup parameters must contain '${SCHEDULE}' or '${SCHEDULE_NAME}'!")
            }
            backupParameterBuilder.scheduleName(backupParameters.get(SCHEDULE_NAME))
        }
        return backupParameterBuilder.build()
    }
}