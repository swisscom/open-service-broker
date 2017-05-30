package com.swisscom.cloud.sb.broker.util

import com.google.common.base.MoreObjects
import com.google.common.collect.MapMaker
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

@Component
@CompileStatic
class SimpleMutexFactory implements MutexFactory {
    private static final ConcurrentMap<String, Lock> locks = new MapMaker().makeMap()

    @Override
    Object getNamedMutex(String name) {
        final Lock lock = new ReentrantLock()
        Lock existing = locks.putIfAbsent(name, lock)
        return MoreObjects.firstNonNull(existing, lock)
    }
}
