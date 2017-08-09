package com.swisscom.cloud.sb.broker

import com.swisscom.cloud.sb.broker.model.repository.NamedLockRepository
import com.swisscom.cloud.sb.broker.util.NamedDistributedMutex
import com.swisscom.cloud.sb.broker.util.StringGenerator
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NamedDistributedMutexSpec extends BaseTransactionalSpecification {
    private static final int N_THREADS = 4
    private final String TEST_LOCKNAME = "NamedSemaphoreSpec"

    @Autowired
    private NamedLockRepository namedLockRepository

    @Autowired
    private final NamedDistributedMutex namedDistributedMutex

    private String id = StringGenerator.randomUuid()

    def "create a bunch of NamedLocks an await these"() {
        given:
        def executor = Executors.newFixedThreadPool(N_THREADS)
        when:
        for (int i = 0; i < N_THREADS * 2; i++) {
            executor.execute(new Runnable() {
                @Override
                void run() {
                    namedDistributedMutex.tryLock(TEST_LOCKNAME, 30, TimeUnit.SECONDS)
                    sleep(2000)
                    namedDistributedMutex.unlock(TEST_LOCKNAME)
                }
            })
        }
        // Wait until all threads are finish
        executor.awaitTermination(60, TimeUnit.SECONDS)
        executor.shutdown

        then:
        noExceptionThrown()
    }
}
