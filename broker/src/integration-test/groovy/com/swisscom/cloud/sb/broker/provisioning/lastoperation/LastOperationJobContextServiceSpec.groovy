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
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import org.springframework.beans.factory.annotation.Autowired

class LastOperationJobContextServiceSpec extends BaseTransactionalSpecification {
    @Autowired
    LastOperationJobContextService jobContextService
    @Autowired
    DBTestUtil dbTestUtil

    def "happy path load context"() {
        given:
        def id = "ServiceInstanceGUID"
        def service = dbTestUtil.createServiceWith2Plans()
        def serviceInstance = dbTestUtil.createServiceInstance(service, id)
        and:
        LastOperation lastOperation = dbTestUtil.createLastOperation(id)
        when:
        LastOperationJobContext lastOperationContext = jobContextService.loadContext(id)
        then:
        lastOperationContext.lastOperation.guid == lastOperation.guid
    }

    def "an exception is thrown when an unknown job context is attempted to load"() {
        when:
        LastOperationJobContext lastOperationContext = jobContextService.loadContext("noSuchId")
        then:
        Exception ex = thrown(RuntimeException)
        ex
    }
}
