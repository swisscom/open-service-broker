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
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ShieldRestClientFactory {

    private List<ShieldRestClient> shieldRestClients

    @Autowired
    ShieldRestClientFactory(List<ShieldRestClient> shieldRestClients) {
        this.shieldRestClients = shieldRestClients
    }

    ShieldRestClient build() {
        for (ShieldRestClient shieldRestClient in shieldRestClients) {
            if (shieldRestClient.matchVersion()) return shieldRestClient
        }
        throw new Exception("No matching shield implementation found")
    }
}