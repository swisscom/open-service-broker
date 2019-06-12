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

package com.swisscom.cloud.sb.broker.backup.shield.restClient

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.util.Assert

/**
 * Strategy pattern to support multiple shield API versions
 * See <a href="https://github.com/shieldproject/shield">Shield Project</a>
 */
// TODO: Is not a factory
@Component
@CompileStatic
class ShieldRestClientFactory {

    private List<ShieldRestClient> shieldRestClients

    @Autowired
    ShieldRestClientFactory(List<ShieldRestClient> shieldRestClients) {
        Assert.notEmpty(shieldRestClients, "At least one ShieldRestClient has to be provided")
        this.shieldRestClients = shieldRestClients
    }

    /**
     * Returns first {@link ShieldRestClient} implementation that returns true when executing
     * {@link ShieldRestClient#matchVersion )}
     */
    ShieldRestClient build() {
        for (ShieldRestClient shieldRestClient in shieldRestClients) {
            if (shieldRestClient.matchVersion()) {
                return shieldRestClient
            }
        }
        throw new Exception("No matching shield implementation found")
    }
}