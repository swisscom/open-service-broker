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
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

@Ignore
class BoshRestClientTest extends BaseSpecification {
    BoshRestClient client
    @Autowired
    MongoDbEnterpriseConfig config


    def setup() {
        client = new BoshRestClient(config, new RestTemplateBuilder())
    }

    def "get info works correctly"() {
        when:
        def info = client.fetchBoshInfo()
        then:
        noExceptionThrown()
    }

    def "get deployment"() {
        when:
        def deployment = client.getDeployment("shield")
        then:
        noExceptionThrown()
    }

    def "get all cloud configs"() {
        when:
        def cc = client.getConfigs(null, 'cloud')
        then:
        noExceptionThrown()
    }

    def "get task"(){
        when:
        def task = client.getTask("114794")
        then:
        noExceptionThrown()
    }
}
