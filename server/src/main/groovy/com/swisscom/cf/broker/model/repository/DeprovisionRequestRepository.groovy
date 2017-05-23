package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.DeprovisionRequest

interface DeprovisionRequestRepository extends BaseRepository<DeprovisionRequest, Integer> {

    DeprovisionRequest findByServiceInstanceGuid(String serviceInstanceGuid)
}