package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.ServiceBinding

interface ServiceBindingRepository extends BaseRepository<ServiceBinding, Integer> {
    ServiceBinding findByGuid(String guid)
}
