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
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.StringGenerator
import org.springframework.beans.factory.annotation.Autowired

class LastOperationJobContextSpec extends BaseTransactionalSpecification {
    private String id = StringGenerator.randomUuid()
    @Autowired
    DBTestUtil dbTestUtil
    @Autowired
    LastOperationRepository lastOperationRepository
    @Autowired
    LastOperationJobContextService lastOperationJobContextService

    def "last operation state and description should be set correctly"() {
        given:
        LastOperationJobContext lastOperationContext = createLastOperationContext()
        when:
        lastOperationContext.notifyResult(success, description)
        then:
        def lastOperation = lastOperationRepository.findByGuid(id)
        lastOperation.status == success ? LastOperation.Status.SUCCESS : LastOperation.Status.FAILED
        lastOperation.description == description
        where:
        success | description
        true    | 'desc1'
        false   | 'desc2'
    }

    def "NotifySuccess works correctly"() {
        given:
        LastOperationJobContext lastOperationContext = createLastOperationContext()
        when:
        lastOperationContext.notifySuccess("blabla")
        then:
        def lastOperation = lastOperationRepository.findByGuid(id)
        lastOperation.status == LastOperation.Status.SUCCESS
        lastOperation.description == 'blabla'
    }

    def "NotifyFailure works correctly"() {
        given:
        LastOperationJobContext lastOperationContext = createLastOperationContext()
        when:
        lastOperationContext.notifyFailure("blabla")
        then:
        def lastOperation = lastOperationRepository.findByGuid(id)
        lastOperation.status == LastOperation.Status.FAILED
        lastOperation.description == 'blabla'
    }

    private LastOperationJobContext createLastOperationContext() {
        dbTestUtil.createLastOperation(id)
        lastOperationJobContextService.loadContext(id)
    }
}
