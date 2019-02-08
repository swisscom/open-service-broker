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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager

import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDBAutomationConfigUpdateNotCompletedException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class OpsManagerOngoingChangesChecker {
    @Autowired
    OpsManagerFacade opsManagerFacade

    @Retryable(
            value = MongoDBAutomationConfigUpdateNotCompletedException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 5000l))
    void checkAndRetryForOnGoingChanges(String groupId) throws MongoDBAutomationConfigUpdateNotCompletedException {
        log.info("Checking for on-going changes")
        if (!opsManagerFacade.isAutomationUpdateComplete(groupId))
            throw new MongoDBAutomationConfigUpdateNotCompletedException()
    }
}
