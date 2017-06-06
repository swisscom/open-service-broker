package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.Backup


interface BackupRepository extends BaseRepository<Backup, Integer> {
    List<Backup> findByServiceInstanceGuid(String serviceInstanceGuid)

    Backup findByGuid(String guid)
}
