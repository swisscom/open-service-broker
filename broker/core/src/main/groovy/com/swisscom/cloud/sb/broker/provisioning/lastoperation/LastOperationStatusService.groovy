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
@Slf4j
@CompileStatic
class LastOperationStatusService {
    @Autowired
    private LastOperationStatus2CFLastOperationStatusConverter converter
    @Autowired
    private LastOperationRepository lastOperationRepository

    LastOperationResponseDto pollJobStatus(String serviceInstanceGuid) {
        LastOperation job = lastOperationRepository.findByGuid(serviceInstanceGuid)
        if (!job) {
            log.debug "LastOperation with id: ${serviceInstanceGuid} does not exist - returning 410 GONE"
            ErrorCode.LAST_OPERATION_NOT_FOUND.throwNew()
        }
        return new LastOperationResponseDto(status: converter.convert(job.status),
                description: job.description)
    }
}
