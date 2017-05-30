package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ProvisionRequest

interface ProvisionRequestRepository extends BaseRepository<ProvisionRequest, Integer> {
    ProvisionRequest findByServiceInstanceGuid(String serviceInstanceGuid)
    Integer deleteByServiceInstanceGuid(String guid)
}