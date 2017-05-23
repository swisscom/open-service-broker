package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.CFService

interface CFServiceRepository extends BaseRepository<CFService, Integer> {
    CFService findByGuid(String guid)
    CFService findByName(String name)
}
