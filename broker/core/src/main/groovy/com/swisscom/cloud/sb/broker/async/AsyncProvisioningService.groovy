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

package com.swisscom.cloud.sb.broker.async

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.job.DeprovisioningJobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.ProvisioningJobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.UpdateJobConfig
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import com.swisscom.cloud.sb.broker.updating.UpdatingPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AsyncProvisioningService {
    private final LastOperationPersistenceService lastOperationService
    private final ProvisioningPersistenceService provisioningPersistenceService
    private final JobManager jobManager
    private final UpdatingPersistenceService updatingPersistenceService

    @Autowired
    AsyncProvisioningService(
            LastOperationPersistenceService lastOperationService,
            ProvisioningPersistenceService provisioningPersistenceService,
            JobManager jobManager,
            UpdatingPersistenceService updatingPersistenceService) {
        this.lastOperationService = lastOperationService
        this.provisioningPersistenceService = provisioningPersistenceService
        this.jobManager = jobManager
        this.updatingPersistenceService = updatingPersistenceService
    }

    void scheduleProvision(ProvisioningJobConfig provisionJobConfig) {
        provisioningPersistenceService.saveProvisionRequest(provisionJobConfig.provisionRequest)
        lastOperationService.createOrUpdateLastOperation(provisionJobConfig.guid, LastOperation.Operation.PROVISION)
        jobManager.queue(provisionJobConfig)
    }

    void scheduleDeprovision(DeprovisioningJobConfig deprovisioningJobConfig) {
        provisioningPersistenceService.saveDeprovisionRequest(deprovisioningJobConfig.deprovisionRequest)
        lastOperationService.createOrUpdateLastOperation(deprovisioningJobConfig.guid, LastOperation.Operation.DEPROVISION)
        jobManager.queue(deprovisioningJobConfig)
    }

    void scheduleUpdate(UpdateJobConfig updateJobConfig) {
        updatingPersistenceService.saveUpdateRequest(updateJobConfig.updateRequest)
        lastOperationService.createOrUpdateLastOperation(updateJobConfig.guid, LastOperation.Operation.UPDATE)
        jobManager.queue(updateJobConfig)
    }
}
