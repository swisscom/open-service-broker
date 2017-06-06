package com.swisscom.cloud.sb.broker.services.bosh

import com.swisscom.cloud.sb.broker.services.bosh.client.BoshClientFactory
import com.swisscom.cloud.sb.broker.services.mongodb.enterprise.openstack.OpenStackClientFactory
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
