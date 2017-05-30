package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.util.MutexFactory
import com.swisscom.cloud.sb.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class BoshClientFactory {

    private final RestTemplateFactory restTemplateFactory
    private final MutexFactory mutexFactory

    @Autowired
    BoshClientFactory(RestTemplateFactory restTemplateFactory, MutexFactory mutexFactory) {
        this.restTemplateFactory = restTemplateFactory
        this.mutexFactory = mutexFactory
    }

    BoshClient build(BoshConfig boshConfig) {
        return new BoshClient(new BoshRestClient(boshConfig, restTemplateFactory), mutexFactory)
    }

    RestTemplateFactory getRestTemplateFactory() {
        return restTemplateFactory
    }

    MutexFactory getMutexFactory() {
        return mutexFactory
    }
}