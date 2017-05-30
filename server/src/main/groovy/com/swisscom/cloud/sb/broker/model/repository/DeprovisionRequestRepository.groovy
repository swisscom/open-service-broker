package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest

interface DeprovisionRequestRepository extends BaseRepository<DeprovisionRequest, Integer> {

    DeprovisionRequest findByServiceInstanceGuid(String serviceInstanceGuid)
}