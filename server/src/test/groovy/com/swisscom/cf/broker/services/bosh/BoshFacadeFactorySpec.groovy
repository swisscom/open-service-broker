package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import com.swisscom.cf.broker.services.bosh.client.BoshClientFactory
import spock.lang.Specification


class BoshFacadeFactorySpec extends Specification {

    def "facade is created correctly"() {
        given:
        BoshClientFactory boshClientFactory = Mock(BoshClientFactory)
        OpenStackClientFactory openStackClientFactory = Mock(OpenStackClientFactory)
        BoshTemplateFactory boshTemplateFactory = Mock(BoshTemplateFactory)
        BoshFacadeFactory boshFacadeFactory = new BoshFacadeFactory(boshClientFactory, openStackClientFactory, boshTemplateFactory)

        BoshBasedServiceConfig boshBasedServiceConfig = new DummyConfig()
        when:
        def result = boshFacadeFactory.build(boshBasedServiceConfig)

        then:
        result.openStackClientFactory == openStackClientFactory
        result.boshClientFactory == boshClientFactory
        result.serviceConfig == boshBasedServiceConfig
        result.boshTemplateFactory == boshTemplateFactory

    }
}
