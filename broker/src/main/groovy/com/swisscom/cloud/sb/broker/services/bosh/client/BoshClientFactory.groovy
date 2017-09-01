package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.util.MutexFactory
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilderFactory
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class BoshClientFactory {

    RestTemplateBuilderFactory restTemplateBuilderFactory
    private final MutexFactory mutexFactory

    @Autowired
    BoshClientFactory(RestTemplateBuilderFactory restTemplateBuilderFactory, MutexFactory mutexFactory) {
        this.restTemplateBuilderFactory = restTemplateBuilderFactory
        this.mutexFactory = mutexFactory
    }

    BoshClient build(BoshConfig boshConfig) {
        return new BoshClient(new BoshRestClient(boshConfig, restTemplateBuilderFactory), mutexFactory)
    }

    MutexFactory getMutexFactory() {
        return mutexFactory
    }
}