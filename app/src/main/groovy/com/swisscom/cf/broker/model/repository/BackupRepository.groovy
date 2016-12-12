package com.swisscom.cf.broker.model.repository

import com.swisscom.cf.broker.model.Backup


interface BackupRepository extends BaseRepository<Backup, Integer> {
    List<Backup> findByServiceInstanceGuid(String serviceInstanceGuid)

    Backup findByGuid(String guid)
}
