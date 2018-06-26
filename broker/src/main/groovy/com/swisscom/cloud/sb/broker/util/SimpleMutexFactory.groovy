/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
