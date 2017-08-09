package com.swisscom.cloud.sb.broker.util

import com.swisscom.cloud.sb.broker.model.NamedLock
import com.swisscom.cloud.sb.broker.model.repository.NamedLockRepository
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

import java.util.concurrent.TimeUnit

@CompileStatic
@Component
@Slf4j
class NamedDistributedMutex {
    private NamedLockRepository namedLockRepository

    @Autowired
    NamedDistributedMutex(NamedLockRepository namedLockRepository) {
        this.namedLockRepository = namedLockRepository
    }

    boolean tryLock(String lockName, long time, TimeUnit unit) {
        long nanosTimeout = unit.toNanos(time)
        final long deadline = System.nanoTime() + nanosTimeout
        while (true) {
            if (System.nanoTime() >= deadline) {
                return false
            }
            namedLockRepository.deleteAllExpiredByName(lockName)
            if (namedLockRepository.findByName(lockName)) {
                sleep(1000)
            } else {
                def namedLock = new NamedLock(name: lockName, ttlInSeconds: 30)
                try {
                    def persistedNamedLock = namedLockRepository.save(namedLock)
                    if (persistedNamedLock) {
                        return true
                    }
                } catch (DataIntegrityViolationException dve) {
                }
            }
        }
        return false
    }

    boolean unlock(String lockName) {
        namedLockRepository.deleteByName(lockName)
    }

}
