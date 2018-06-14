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
