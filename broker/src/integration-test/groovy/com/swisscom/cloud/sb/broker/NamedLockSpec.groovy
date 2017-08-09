package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.model.NamedLock
import com.swisscom.cloud.sb.broker.model.repository.NamedLockRepository
import com.swisscom.cloud.sb.broker.util.ServiceLifeCycler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

class NamedLockSpec extends BaseTransactionalSpecification {
    @Autowired
    private NamedLockRepository namedLockRepository

    @Autowired
    private ServiceLifeCycler serviceLifeCycler

    private final String TEST_LOCKNAME = "NamedLockSpec"

    def "create and remove unexpired lock should keep it"() {
        given:
        def lock = new NamedLock(name: TEST_LOCKNAME, ttlInSeconds: 10)
        def persistedLock = namedLockRepository.save(lock)
        def lockCountBefore = namedLockRepository.findAll().size()

        when:
        serviceLifeCycler.pauseExecution(5)
        namedLockRepository.deleteAllExpiredByName(TEST_LOCKNAME)

        then:
        noExceptionThrown()
        lockCountBefore == namedLockRepository.findAll().size()
    }

    def "create duplicate lock should throw exception"() {
        given:
        def lock1 = new NamedLock(name: TEST_LOCKNAME, ttlInSeconds: 10)
        def persistedLock1 = namedLockRepository.save(lock1)
        def lockCountBefore = namedLockRepository.findAll().size()

        when:
        def lock2 = new NamedLock(name: TEST_LOCKNAME, ttlInSeconds: 10)
        def persistedLock2 = namedLockRepository.save(lock2)

        then:
        thrown(DataIntegrityViolationException)
    }

    def "create and remove expired lock should remove it"() {
        given:
        def lock = new NamedLock(name: TEST_LOCKNAME, ttlInSeconds: 10)
        def persistedLock = namedLockRepository.save(lock)
        def lockCountBefore = namedLockRepository.findAll().size()

        when:
        serviceLifeCycler.pauseExecution(10)
        namedLockRepository.deleteAllExpiredByName(TEST_LOCKNAME)

        then:
        noExceptionThrown()
        lockCountBefore > namedLockRepository.findAll().size()
    }

}
