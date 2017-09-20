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
        def deployment = client.getDeployment("d-8tlihzux5s3nq370")
        then:
        noExceptionThrown()
    }

    def "get cloud config"() {
        when:
        def cc = client.fetchCloudConfig()
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
