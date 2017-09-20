package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import org.springframework.beans.factory.annotation.Autowired


class RestTemplateBuilderSpec extends BaseTransactionalSpecification {
    @Autowired
    RestTemplateBuilder instance1

    @Autowired
    RestTemplateBuilder instance2

    def 'RestTemplateBuilder instances are injected as prototype'() {
        expect:
        instance1 != instance2
    }
}
