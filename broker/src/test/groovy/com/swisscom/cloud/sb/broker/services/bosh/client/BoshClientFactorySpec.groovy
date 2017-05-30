package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.DummyConfig
import com.swisscom.cloud.sb.broker.util.MutexFactory
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import spock.lang.Specification


class BoshClientFactorySpec extends Specification {
    BoshClientFactory boshClientFactory

    def setup() {
        boshClientFactory = new BoshClientFactory(Stub(RestTemplateFactory), Stub(MutexFactory))
    }

    def "happy path:build bosh client"() {
        given:
        def config = new DummyConfig()
        when:
        BoshClient client = boshClientFactory.build(config)
        then:
        client.mutexFactory == boshClientFactory.mutexFactory
        client.boshRestClient.restTemplateFactory == boshClientFactory.restTemplateFactory
        client.boshConfig == config
    }
}
