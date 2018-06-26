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
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import com.swisscom.cloud.sb.broker.util.test.ErrorCodeHelper
import org.springframework.beans.factory.annotation.Autowired

class LastOperationStatusServiceSpec extends BaseTransactionalSpecification {
    @Autowired
    LastOperationStatusService lastOperationStatusService
    @Autowired
    DBTestUtil dbTestUtil

    def "happy case: last operation status is returned correctly"() {
        given:
        def id = 'someId'
        LastOperation lastOperation = dbTestUtil.createLastOperation(id, LastOperation.Status.IN_PROGRESS)
        when:
        def dto = lastOperationStatusService.pollJobStatus(id)
        then:
        dto.status == CFLastOperationStatus.IN_PROGRESS
    }

    def "when the last operation status is not found an exception should be thrown"() {
        when:
        lastOperationStatusService.pollJobStatus('someUnknownId')
        then:
        Exception ex = thrown(ServiceBrokerException)
        ErrorCodeHelper.assertServiceBrokerException(ex, ErrorCode.LAST_OPERATION_NOT_FOUND)
    }
}
