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

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import org.springframework.beans.factory.annotation.Autowired

class LastOperationServiceSpec extends BaseTransactionalSpecification {
    @Autowired
    LastOperationPersistenceService lastOperationPersistenceService
    @Autowired
    DBTestUtil dbTestUtil
    @Autowired
    LastOperationRepository lastOperationRepository
    def "updating a LastOperation that is 'in progress' state throws an exception"() {
        given:
        String id = "lastOperationId"
        LastOperation lastOperation = lastOperationRepository.save( new LastOperation(guid: id, operation: LastOperation.Operation.PROVISION, dateCreation: new Date(), status: LastOperation.Status.IN_PROGRESS))
        when:
        lastOperationPersistenceService.createOrUpdateLastOperation(id, LastOperation.Operation.DEPROVISION)
        then:
        def ex = thrown(ServiceBrokerException)
        ex.message == ErrorCode.OPERATION_IN_PROGRESS.description
    }

    def "updating an existing and completed LastOperation functions correctly"() {
        given:
        String id = "lastOperationId"
        LastOperation lastOperation = lastOperationRepository.save(new LastOperation(guid: id, operation: LastOperation.Operation.PROVISION, dateCreation: new Date(), status: LastOperation.Status.SUCCESS))
        when:
        lastOperationPersistenceService.createOrUpdateLastOperation(id, LastOperation.Operation.DEPROVISION)
        then:
        lastOperationRepository.findByGuid(id).operation == LastOperation.Operation.DEPROVISION
    }

    def "inserting a brand new LastOperation functions correctly"() {
        given:
        String id = "lastOperationId"
        assert lastOperationRepository.findByGuid(id) == null
        when:
        lastOperationPersistenceService.createOrUpdateLastOperation(id, LastOperation.Operation.PROVISION)
        then:
        lastOperationRepository.findByGuid(id).operation == LastOperation.Operation.PROVISION
    }
}
