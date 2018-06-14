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

package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.util.DummyConfig
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

@Ignore
class BoshClientTest extends BaseSpecification {
    @Autowired
    BoshClientFactory boshClientFactory

    def "get & upload the same cloud config"() {
        given:
        def cloudConfig = createClient().fetchCloudConfig()
        when:

        createClient().updateCloudConfig(cloudConfig.properties)
        then:
        noExceptionThrown()
    }

    def "add new vm"() {
        when:
        createClient().addOrUpdateVmInCloudConfig('test', 'mongoent.small', 'test')
        then:
        noExceptionThrown()
    }

    def createClient() {
        return boshClientFactory.build(new DummyConfig(boshDirectorBaseUrl: "https://localhost:25555",
                boshDirectorUsername: "admin",
                boshDirectorPassword: "admin"))
    }

}
