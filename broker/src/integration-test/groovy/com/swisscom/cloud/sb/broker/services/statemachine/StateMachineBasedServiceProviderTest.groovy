package com.swisscom.cloud.sb.broker.services.statemachine

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineBasedServiceProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class StateMachineBasedServiceProviderTest extends BaseSpecification {
    private static final Logger LOG = LoggerFactory.getLogger(StateMachineBasedServiceProvider.class);

    @Autowired
    DummyStateMachineBasedServiceProvider sut

    def "service provider bean could be found"() {
        when:
        LOG.info("HelloWorldStateMachineBasedServiceProvider bean should be loaded")

        then:
        sut != null
        sut instanceof DummyStateMachineBasedServiceProvider
    }
}
