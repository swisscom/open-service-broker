package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.ProvisionRequest

interface ProvisionRequestRepository extends BaseRepository<ProvisionRequest, Integer> {
    ProvisionRequest findByServiceInstanceGuid(String serviceInstanceGuid)
    Integer deleteByServiceInstanceGuid(String guid)
}