package com.swisscom.cf.broker.services.bosh.client

import com.swisscom.cf.broker.BaseSpecification
import com.swisscom.cf.broker.services.mongodb.enterprise.MongoDbEnterpriseConfig
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

@Ignore
class BoshRestClientTest extends BaseSpecification {
    BoshRestClient client
    @Autowired
    MongoDbEnterpriseConfig config


    def setup() {
        client = new BoshRestClient(config, new RestTemplateFactory())
    }
    def "get info works correctly"() {
        when:
        def info = client.boshInfo
        then:
        noExceptionThrown()
    }

    def "get deployment"() {
        when:
        def deployment = client.getDeployment("d-8tlihzux5s3nq370")
        then:
        noExceptionThrown()
    }

    def "get cloud config"() {
        when:
        def cc = client.getCloudConfig()
        then:
        noExceptionThrown()
    }

    def "get task"(){
        when:
        def task = client.getTask("2857986")
        then:
        noExceptionThrown()
    }
}
