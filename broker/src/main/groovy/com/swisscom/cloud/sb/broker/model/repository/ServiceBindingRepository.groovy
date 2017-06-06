package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.ServiceBinding

interface ServiceBindingRepository extends BaseRepository<ServiceBinding, Integer> {
    ServiceBinding findByGuid(String guid)
}
