/*
 * Copyright (c) 2019 Swisscom (Switzerland) Ltd.
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

package com.swisscom.cloud.sb.broker.services

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.DeprovisionRequestRepository
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.model.repository.ProvisionRequestRepository
import com.swisscom.cloud.sb.broker.model.repository.UpdateRequestRepository
import groovy.transform.CompileStatic
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Service

@Service
@CompileStatic
class LastOperationService {

    private final JobManager jobManager
    private final LastOperationRepository lastOperationRepository
    private final ProvisionRequestRepository provisionRequestRepository
    private final DeprovisionRequestRepository deprovisionRequestRepository
    private final UpdateRequestRepository updateRequestRepository

    LastOperationService(
            JobManager jobManager,
            LastOperationRepository lastOperationRepository,
            ProvisionRequestRepository provisionRequestRepository,
            DeprovisionRequestRepository deprovisionRequestRepository,
            UpdateRequestRepository updateRequestRepository) {
        this.updateRequestRepository = updateRequestRepository
        this.deprovisionRequestRepository = deprovisionRequestRepository
        this.provisionRequestRepository = provisionRequestRepository
        this.lastOperationRepository = lastOperationRepository
        this.jobManager = jobManager
    }

    void terminateLastOperation(String lastOperationGuid) {
        assert StringUtils.isNotEmpty(lastOperationGuid)

        LastOperation lastOperation = lastOperationRepository.findByGuid(lastOperationGuid)
        assert lastOperation != null

        if (lastOperation.status != LastOperation.Status.IN_PROGRESS) {
            return
        }

        jobManager.dequeue(lastOperationGuid)

        lastOperation.status = LastOperation.Status.FAILED
        this.lastOperationRepository.save(lastOperation)

        switch(lastOperation.operation) {
            case LastOperation.Operation.PROVISION:
                def request = provisionRequestRepository.findByServiceInstanceGuid(lastOperationGuid)
                provisionRequestRepository.delete(request)
                break
            case LastOperation.Operation.DEPROVISION:
                def request = deprovisionRequestRepository.findByServiceInstanceGuid(lastOperationGuid)
                deprovisionRequestRepository.delete(request)
                break
            case LastOperation.Operation.UPDATE:
                def request = updateRequestRepository.findByServiceInstanceGuid(lastOperationGuid).last()
                updateRequestRepository.delete(request)
                break
        }

        return
    }
}
