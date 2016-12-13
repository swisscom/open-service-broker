package com.swisscom.cf.broker.async.lastoperation

import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.model.repository.LastOperationRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Log4j
class LastOperationPersistenceService {
    @Autowired
    private LastOperationRepository lastOperationRepository

    LastOperation createOrUpdateLastOperation(String id, LastOperation.Operation operation) {
        LastOperation lastOperation = lastOperationRepository.findByGuid(id)
        if (lastOperation) {
            log.info("For LastOperation Id:${id}, there is an existing entry: ${lastOperation.toString()}")
            assertPreviousOperationCompletion(lastOperation, id, operation)
            lastOperation.operation = operation
            lastOperation.dateCreation = new Date()
            lastOperation.status = LastOperation.Status.IN_PROGRESS
            lastOperation.internalState = null
        } else {
            lastOperation = new LastOperation(guid: id, operation: operation, dateCreation: new Date(), status: LastOperation.Status.IN_PROGRESS)
        }
        lastOperationRepository.save(lastOperation)
        return lastOperation
    }

    void deleteLastOpeation(String guid) {
        lastOperationRepository.deleteByGuid(guid)
    }

    private void assertPreviousOperationCompletion(LastOperation lastOperation, String id, LastOperation.Operation operation) {
        if (lastOperation.status == LastOperation.Status.IN_PROGRESS && lastOperation.operation != operation) {
            throw new IllegalStateException("The previous operation(${lastOperation.operation}) for ${id} has not been finished yet!")
        }
    }
}
