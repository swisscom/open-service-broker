package com.swisscom.cf.broker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext

class ServiceBrokerApplicationTest extends BaseTransactionalSpecification {
    @Autowired
    WebApplicationContext context

    def 'spring context loads'() {
        expect:
        context != null
    }
}
