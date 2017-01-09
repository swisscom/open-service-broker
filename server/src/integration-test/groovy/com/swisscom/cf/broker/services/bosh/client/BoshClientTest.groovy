package com.swisscom.cf.broker.services.bosh.client

import com.swisscom.cf.broker.BaseSpecification
import com.swisscom.cf.broker.util.DummyConfig
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
