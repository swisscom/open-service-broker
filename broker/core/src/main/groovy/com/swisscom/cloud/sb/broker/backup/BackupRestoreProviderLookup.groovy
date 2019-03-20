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

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BackupRestoreProviderLookup {
    private final ServiceProviderLookup serviceProviderLookup

    @Autowired
    BackupRestoreProviderLookup(ServiceProviderLookup serviceProviderLookup) {
        this.serviceProviderLookup = serviceProviderLookup
    }

    BackupRestoreProvider findBackupProvider(Plan plan) {
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(plan)
        if (!(serviceProvider instanceof BackupRestoreProvider)) {
            ErrorCode.BACKUP_NOT_ENABLED.throwNew()
        }
        return serviceProvider as BackupRestoreProvider
    }

    boolean isBackupProvider(Plan plan) {
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(plan)
        return (serviceProvider instanceof BackupRestoreProvider)
    }
}