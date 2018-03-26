package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.UpdateRequest

interface UpdateRequestRepository extends BaseRepository<UpdateRequest, Integer> {
    List<UpdateRequest> findByServiceInstanceGuid(String serviceInstanceGuid)
}