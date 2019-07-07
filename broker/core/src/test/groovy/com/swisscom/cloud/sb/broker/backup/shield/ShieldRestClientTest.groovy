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

package com.swisscom.cloud.sb.broker.backup.shield


import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@Ignore
class ShieldRestClientTest extends Specification {
    ShieldRestClientV1 restClient

    void setup() {
        ShieldConfig shieldConfig = new ShieldConfig()
        shieldConfig.baseUrl = "https://localhost:8443"
        shieldConfig.username = "admin"
        shieldConfig.password = "shield"
        shieldConfig.defaultTenantName = "tenant1"
        shieldConfig.apiKey = "averyhardkey"
        restClient = ShieldRestClientV1.of(shieldConfig)
    }

    def "get store by name"() {
        when:
        def store = restClient.getStoreByName("default")
        then:
        store
    }

    def "get store by name not found"() {
        when:
        def store = restClient.getStoreByName("notexisting")
        then:
        store == null
    }

    def "get retention by name"() {
        when:
        def retention = restClient.getRetentionByName("default")
        then:
        retention
    }

    def "get retention by name not found"() {
        when:
        def retention = restClient.getRetentionByName("notexisting")
        then:
        retention == null
    }
}
