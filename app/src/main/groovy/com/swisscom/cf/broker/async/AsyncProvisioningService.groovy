package com.swisscom.cf.broker.async

import com.swisscom.cf.broker.async.job.JobManager
import com.swisscom.cf.broker.async.job.config.DeprovisioningJobConfig
import com.swisscom.cf.broker.async.job.config.ProvisioningjobConfig
import com.swisscom.cf.broker.async.lastoperation.LastOperationPersistenceService
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.ProvisioningPersistenceService
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
