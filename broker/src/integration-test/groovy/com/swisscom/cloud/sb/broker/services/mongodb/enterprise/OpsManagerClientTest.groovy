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

package com.swisscom.cloud.sb.broker.services.mongodb.enterprise

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.dto.alert.AlertConfigsDto
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.opsmanager.OpsManagerClient
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

@Ignore
class OpsManagerClientTest extends BaseSpecification {
    @Autowired
    OpsManagerClient opsManagerClient

    def "test user deletion"() {
        when:
        opsManagerClient.deleteUser("572744aae4b0b7951b482272")
        then:
        noExceptionThrown()
    }

    def "get and set automation config"() {
        given:
        def groupId = "57298f2ee4b0b7951b85807d"
        def automationConfig = opsManagerClient.getAutomationConfig(groupId)
        def goalBefore = opsManagerClient.getAutomationStatus(groupId).goalVersion
        when:
        def updatedStatus = opsManagerClient.updateAutomationConfig(groupId, automationConfig)
        def goalAfter = opsManagerClient.getAutomationStatus(groupId).goalVersion
        then:
        updatedStatus.version > automationConfig.version
        goalAfter > goalBefore
    }

    def "get clusterId"() {
        given:
        def groupId = '573f1d36e4b070b97d26df01'

        when:
        def result = opsManagerClient.getClusterId(groupId, 'rs_6d65b38b-b665-4050-b905-53678c2dab49')
        println(result)

        then:
        result
    }

    def "get userId by name"() {
        when:
        def id = opsManagerClient.getUserByName('admin')
        println(id)
        then:
        id
    }

    def "get alertConfigs"() {
        given:
        def groupId = '5b86593eb61cea1a95e55405'

        when:
        def result = opsManagerClient.listAlerts(groupId)
        result.results.each {println(it.id)}

        then:
        result.class == AlertConfigsDto
        result.results != null
    }

    def "delete alertConfig"() {
        given:
        def groupId = '5b86593eb61cea1a95e55405'
        def id = '5b865af1b61cea1a95e56164'

        when:
        def result = opsManagerClient.deleteAlertConfig(groupId, id)
        println(result)

        then:
        result
    }

    def "delete non existing alertConfig"() {
        given:
        def groupId = '5b86593eb61cea1a95e55405'
        def id = 'notExistingId'

        when:
        def result = opsManagerClient.deleteAlertConfig(groupId, id)
        println(result)

        then:
        result == false
    }
}