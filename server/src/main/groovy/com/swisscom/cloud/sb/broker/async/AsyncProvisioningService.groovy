package com.swisscom.cloud.sb.broker.async

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.provisioning.job.DeprovisioningJobConfig
import com.swisscom.cloud.sb.broker.provisioning.job.ProvisioningjobConfig
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import com.swisscom.cloud.sb.broker.provisioning.lastoperation.LastOperationPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AsyncProvisioningService {
    private final LastOperationPersistenceService lastOperationService
    private final ProvisioningPersistenceService provisioningPersistenceService
    private final JobManager jobManager

    @Autowired
    AsyncProvisioningService(LastOperationPersistenceService lastOperationService, ProvisioningPersistenceService provisioningPersistenceService, JobManager jobManager) {
        this.lastOperationService = lastOperationService
        this.provisioningPersistenceService = provisioningPersistenceService
        this.jobManager = jobManager
    }

    void scheduleProvision(ProvisioningjobConfig provisionjobConfig) {
        provisioningPersistenceService.saveProvisionRequest(provisionjobConfig.provisionRequest)
        lastOperationService.createOrUpdateLastOperation(provisionjobConfig.guid, LastOperation.Operation.PROVISION)
        jobManager.queue(provisionjobConfig)
    }

    void scheduleDeprovision(DeprovisioningJobConfig deprovisioningJobConfig) {
        provisioningPersistenceService.saveDeprovisionRequest(deprovisioningJobConfig.deprovisionRequest)
        lastOperationService.createOrUpdateLastOperation(deprovisioningJobConfig.guid, LastOperation.Operation.DEPROVISION)
        jobManager.queue(deprovisioningJobConfig)
    }
}
