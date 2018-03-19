package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.BaseTransactionalSpecification
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import com.swisscom.cloud.sb.broker.util.DBTestUtil
import org.springframework.beans.factory.annotation.Autowired

class LastOperationServiceSpec extends BaseTransactionalSpecification {
    @Autowired
    LastOperationPersistenceService lastOperationPersistenceService
    @Autowired
    DBTestUtil dbTestUtil
    @Autowired
    LastOperationRepository lastOperationRepository
    def "updating a LastOperation that is 'in progress' state throws an exception"() {
        given:
        String id = "lastOperationId"
        LastOperation lastOperation = lastOperationRepository.save( new LastOperation(guid: id, operation: LastOperation.Operation.PROVISION, dateCreation: new Date(), status: LastOperation.Status.IN_PROGRESS))
        when:
        lastOperationPersistenceService.createOrUpdateLastOperation(id, LastOperation.Operation.DEPROVISION)
        then:
        def ex = thrown(ServiceBrokerException)
        ex.message == ErrorCode.OPERATION_IN_PROGRESS.description
    }

    def "updating an existing and completed LastOperation functions correctly"() {
        given:
        String id = "lastOperationId"
        LastOperation lastOperation = lastOperationRepository.save(new LastOperation(guid: id, operation: LastOperation.Operation.PROVISION, dateCreation: new Date(), status: LastOperation.Status.SUCCESS))
        when:
        lastOperationPersistenceService.createOrUpdateLastOperation(id, LastOperation.Operation.DEPROVISION)
        then:
        lastOperationRepository.findByGuid(id).operation == LastOperation.Operation.DEPROVISION
    }

    def "inserting a brand new LastOperation functions correctly"() {
        given:
        String id = "lastOperationId"
        assert lastOperationRepository.findByGuid(id) == null
        when:
        lastOperationPersistenceService.createOrUpdateLastOperation(id, LastOperation.Operation.PROVISION)
        then:
        lastOperationRepository.findByGuid(id).operation == LastOperation.Operation.PROVISION
    }
}
