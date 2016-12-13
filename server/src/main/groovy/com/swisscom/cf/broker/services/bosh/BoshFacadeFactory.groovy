package com.swisscom.cf.broker.services.bosh

import com.swisscom.cf.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
import com.swisscom.cf.broker.services.bosh.client.BoshClientFactory
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class BoshFacadeFactory {
    private final BoshClientFactory boshClientFactory
    private final OpenStackClientFactory openStackClientFactory
    private final BoshTemplateFactory boshTemplateFactory

    @Autowired
    BoshFacadeFactory(BoshClientFactory boshClientFactory, OpenStackClientFactory openStackClientFactory, BoshTemplateFactory boshTemplateFactory) {
        this.boshClientFactory = boshClientFactory
        this.openStackClientFactory = openStackClientFactory
        this.boshTemplateFactory = boshTemplateFactory
    }

    BoshFacade build(BoshBasedServiceConfig boshBasedServiceConfig) {
        return new BoshFacade(boshClientFactory, openStackClientFactory, boshBasedServiceConfig, boshTemplateFactory)
    }
}
