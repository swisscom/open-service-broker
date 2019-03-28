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

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@CompileStatic
@Transactional
class LastOperationJobContextService {
    @Autowired
    private LastOperationRepository lastOperationRepository
    @Autowired
    private ApplicationContext context

    LastOperationJobContext loadContext(String guid) {
        LastOperation lastOperation = lastOperationRepository.findByGuid(guid)
        if (!lastOperation) {
            throw new RuntimeException("Could not load LastOperation with id:${guid}")
        }

        def context = context.getBean(LastOperationJobContext.class)
        context.lastOperation = lastOperation
        return context
    }
}
