package com.swisscom.cloud.sb.broker.services.statemachine

import com.swisscom.cloud.sb.broker.provisioning.statemachine.StateMachineBasedServiceProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(properties = "spring.autoconfigure.exclude=com.swisscom.cloud.sb.broker.util.httpserver.WebSecurityConfig")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "com.swisscom.cloud.sb.broker.util.httpserver.*"))
class StateMachineBasedServiceProviderTest extends Specification {
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
