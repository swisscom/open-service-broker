package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.services.bosh.BoshConfig
import com.swisscom.cloud.sb.broker.util.MutexFactory
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class BoshClientFactory {

    RestTemplateBuilder restTemplateBuilder
    private final MutexFactory mutexFactory

    @Autowired
    BoshClientFactory(RestTemplateBuilder restTemplateBuilder, MutexFactory mutexFactory) {
        this.restTemplateBuilder = restTemplateBuilder
        this.mutexFactory = mutexFactory
    }

    BoshClient build(BoshConfig boshConfig) {
        return new BoshClient(new BoshRestClient(boshConfig, restTemplateBuilder), mutexFactory)
    }

    MutexFactory getMutexFactory() {
        return mutexFactory
    }
}