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
