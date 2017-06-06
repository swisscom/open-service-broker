package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.swisscom.cloud.sb.broker.model.LastOperation
import com.swisscom.cloud.sb.broker.model.repository.LastOperationRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@CompileStatic
@Transactional
class LastOperationJobContextService {
    @Autowired
    private LastOperationRepository lastOperationRepository
    @Autowired
    private ApplicationContext context

    LastOperationJobContext loadContext(String guid) {
        LastOperation lastOperation = lastOperationRepository.findByGuid(guid)
        if (!lastOperation) {
            throw new RuntimeException("Could not load LastOperation with id:${guid}")
        }

        def context = context.getBean(LastOperationJobContext.class)
        context.lastOperation = lastOperation
        return context
    }
}
