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

package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.repository.DeprovisionRequestRepository
import com.swisscom.cloud.sb.broker.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.repository.ProvisionRequestRepository
import com.swisscom.cloud.sb.broker.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.services.LastOperationService
import spock.lang.Specification

class LastOperationServiceSpec extends Specification {

    LastOperationRepository lastOperationRepository
    JobManager jobManager
    ProvisionRequestRepository provisionRequestRepository
    DeprovisionRequestRepository deprovisionRequestRepository
    UpdateRequestRepository updateRequestRepository

    LastOperationService testee

    void setup() {
        lastOperationRepository = Mock(LastOperationRepository)
        jobManager = Mock(JobManager)
        provisionRequestRepository = Mock(ProvisionRequestRepository)
        deprovisionRequestRepository = Mock(DeprovisionRequestRepository)
        updateRequestRepository = Mock(UpdateRequestRepository)

        testee = new LastOperationService(jobManager, lastOperationRepository, provisionRequestRepository, deprovisionRequestRepository, updateRequestRepository)

    }

    void 'Can terminate provisioning last operation'() {
        given:
        String serviceInstanceGuid = UUID.randomUUID().toString()
        def lastOperation = new LastOperation(
                guid: serviceInstanceGuid,
                operation: LastOperation.Operation.PROVISION,
                status: LastOperation.Status.IN_PROGRESS
        )
        1 * lastOperationRepository.findByGuid(serviceInstanceGuid) >> lastOperation
        1 * jobManager.dequeue(serviceInstanceGuid)
        1 * provisionRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
        1 * provisionRequestRepository.delete(_)
        0 * deprovisionRequestRepository.delete(_)
        0 * updateRequestRepository.delete(_)

        when:
        testee.terminateLastOperation(serviceInstanceGuid)

        then:
        noExceptionThrown()
        lastOperation.status == LastOperation.Status.FAILED
    }

    void 'Can terminate update last operation'() {
        given:
        String serviceInstanceGuid = UUID.randomUUID().toString()
        def lastOperation = new LastOperation(
                guid: serviceInstanceGuid,
                operation: LastOperation.Operation.UPDATE,
                status: LastOperation.Status.IN_PROGRESS
        )
        1 * lastOperationRepository.findByGuid(serviceInstanceGuid) >> lastOperation
        1 * jobManager.dequeue(serviceInstanceGuid)
        1 * updateRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid) >> [ new UpdateRequest() ]
        0 * provisionRequestRepository.delete(_)
        0 * deprovisionRequestRepository.delete(_)
        1 * updateRequestRepository.delete(_)

        when:
        testee.terminateLastOperation(serviceInstanceGuid)

        then:
        noExceptionThrown()
        lastOperation.status == LastOperation.Status.FAILED
    }


    void 'Can terminate deprovision last operation'() {
        given:
        String serviceInstanceGuid = UUID.randomUUID().toString()
        def lastOperation = new LastOperation(
                guid: serviceInstanceGuid,
                operation: LastOperation.Operation.DEPROVISION,
                status: LastOperation.Status.IN_PROGRESS
        )
        1 * lastOperationRepository.findByGuid(serviceInstanceGuid) >> lastOperation
        1 * jobManager.dequeue(serviceInstanceGuid)
        0 * provisionRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
        0 * provisionRequestRepository.delete(_)
        1 * deprovisionRequestRepository.delete(_)
        0 * updateRequestRepository.delete(_)

        when:
        testee.terminateLastOperation(serviceInstanceGuid)

        then:
        noExceptionThrown()
        lastOperation.status == LastOperation.Status.FAILED
    }


    void 'Does nothing if last operation is not in progress anymore'() {
        given:
        String serviceInstanceGuid = UUID.randomUUID().toString()
        def lastOperation = new LastOperation(
                guid: serviceInstanceGuid,
                operation: LastOperation.Operation.PROVISION,
                status: LastOperation.Status.FAILED
        )
        1 * lastOperationRepository.findByGuid(serviceInstanceGuid) >> lastOperation
        0 * jobManager.dequeue(serviceInstanceGuid)
        0 * provisionRequestRepository.findByServiceInstanceGuid(serviceInstanceGuid)
        0 * provisionRequestRepository.delete(_)
        0 * deprovisionRequestRepository.delete(_)
        0 * updateRequestRepository.delete(_)

        when:
        testee.terminateLastOperation(serviceInstanceGuid)

        then:
        noExceptionThrown()
        lastOperation.status == LastOperation.Status.FAILED
    }
}
