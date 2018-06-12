package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.CFService

interface CFServiceRepository extends BaseRepository<CFService, Integer> {
    CFService findByGuid(String guid)

    CFService findByName(String name)
}
