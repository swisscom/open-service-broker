package com.swisscom.cloud.sb.broker.util

import spock.lang.Specification


class SimpleLockFactorySpec extends Specification {
    SimpleMutexFactory simpleLockFactory = new SimpleMutexFactory()

    def "when the same name is used for a lock, the returned objects should be the same"() {
        given:
        String lockName = 'name'
        when:
        def lock1 = simpleLockFactory.getNamedMutex(lockName)
        def lock2 = simpleLockFactory.getNamedMutex(lockName)
        then:
        lock1 == lock2
    }

    def "when different names are used for a lock, the returned objects should *NOT* be the same"() {
        when:
        def lock1 = simpleLockFactory.getNamedMutex("lockName1")
        def lock2 = simpleLockFactory.getNamedMutex("lockName2")
        then:
        lock1 != lock2
    }
}
