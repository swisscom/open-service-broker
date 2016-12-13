package com.swisscom.cf.broker.services.mongodb.enterprise

import com.swisscom.cf.broker.BaseSpecification
import com.swisscom.cf.broker.services.mongodb.enterprise.opsmanager.OpsManagerClient
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
}
