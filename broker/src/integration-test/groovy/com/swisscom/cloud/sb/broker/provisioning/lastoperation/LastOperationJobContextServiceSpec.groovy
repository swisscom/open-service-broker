package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import org.springframework.beans.factory.annotation.Autowired

class LastOperationJobContextServiceSpec extends BaseTransactionalSpecification {
    @Autowired
    LastOperationJobContextService jobContextService
    @Autowired
    DBTestUtil dbTestUtil

    def "happy path load context"() {
        given:
        def id = "ServiceInstanceGUID"
        def service = dbTestUtil.createServiceWith2Plans()
        def serviceInstance = dbTestUtil.createServiceInstace(service, id)
        and:
        LastOperation lastOperation = dbTestUtil.createLastOperation(id)
        when:
        LastOperationJobContext lastOperationContext = jobContextService.loadContext(id)
        then:
        lastOperationContext.lastOperation.guid == lastOperation.guid
    }

    def "an exception is thrown when an unknown job context is attempted to load"() {
        when:
        LastOperationJobContext lastOperationContext = jobContextService.loadContext("noSuchId")
        then:
        Exception ex = thrown(RuntimeException)
        ex
    }
}
