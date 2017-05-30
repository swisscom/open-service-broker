package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.Restore

interface RestoreRepository extends BaseRepository<Restore, Integer> {
    Restore findByGuid(String guid)
}
