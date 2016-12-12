package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.LastOperation
import org.springframework.transaction.annotation.Transactional

interface LastOperationRepository extends BaseRepository<LastOperation, Integer> {
    LastOperation findByGuid(String guid)
    @Transactional
    Integer deleteByGuid(String guid)
}
