package com.swisscom.cf.broker.services.mongodb.enterprise.openstack

import com.swisscom.cf.broker.BaseSpecification
import com.swisscom.cf.broker.util.StringGenerator
import spock.lang.Ignore

@Ignore
class OpenStackClientTest extends BaseSpecification {

    private def createClient() {
        new OpenStackClient("https://localhost:5000/v2.0", 'test','test','test')
    }

    def "listing server groups functions correctly"() {
        when:
        def result = createClient().listServerGroups()
        then:
        noExceptionThrown()
    }

    def "registering server group functions correctly"() {
        given:
        def serverGroup = StringGenerator.randomUuid()
        when:
        createClient().createServerGroup(serverGroup, OpenStackClient.POLICY_ANTI_AFFINITY)
        then:
        noExceptionThrown()
        cleanup:
        createClient().deleteServerGroup(serverGroup)
    }
}
