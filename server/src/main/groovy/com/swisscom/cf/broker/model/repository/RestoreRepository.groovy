package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.Restore

interface RestoreRepository extends BaseRepository<Restore, Integer> {
    Restore findByGuid(String guid)
}
