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

package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class LastOperationPersistenceService {
    @Autowired
    private LastOperationRepository lastOperationRepository

    LastOperation createOrUpdateLastOperation(String id, LastOperation.Operation operation) {
        LastOperation lastOperation = lastOperationRepository.findByGuid(id)
        if (lastOperation) {
            log.info("For LastOperation Id:${id}, there is an existing entry: ${lastOperation.toString()}")
            failIfPreviousOperationIsNotComplete(lastOperation)
            lastOperation.operation = operation
            lastOperation.dateCreation = new Date()
            lastOperation.status = LastOperation.Status.IN_PROGRESS
            lastOperation.internalState = null
        } else {
            lastOperation = new LastOperation(guid: id, operation: operation, dateCreation: new Date(), status: LastOperation.Status.IN_PROGRESS)
        }
        lastOperationRepository.saveAndFlush(lastOperation)
        return lastOperation
    }

    void deleteLastOperation(String guid) {
        lastOperationRepository.deleteByGuid(guid)
    }

    private void failIfPreviousOperationIsNotComplete(LastOperation lastOperationOnServiceInstance) {
        if (lastOperationOnServiceInstance && lastOperationOnServiceInstance.status == LastOperation.Status.IN_PROGRESS)
            ErrorCode.OPERATION_IN_PROGRESS.throwNew()
    }
}
