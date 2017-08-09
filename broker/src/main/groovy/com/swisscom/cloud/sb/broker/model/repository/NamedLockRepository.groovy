package com.swisscom.cloud.sb.broker.model.repository

import com.swisscom.cloud.sb.broker.model.NamedLock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface NamedLockRepository extends BaseRepository<NamedLock, Integer> {
    NamedLock findByName(String name)

    @Query("SELECT nl FROM NamedLock nl WHERE nl.dateCreated <= NOW()-nl.ttlInSeconds AND nl.name=:lockName")
    List<NamedLock> findAllExpiredLocks(@Param("lockName") String lockName)

    @Transactional
    void deleteByName(String name)

    @Transactional
    @Modifying
    @Query("DELETE FROM NamedLock nl WHERE nl.dateCreated <= NOW()-nl.ttlInSeconds AND nl.name=:lockName")
    void deleteAllExpiredByName(@Param("lockName") String lockName)
}
