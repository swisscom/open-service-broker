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


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean

class LoggingRequestInterceptorSpec extends Specification {
    RestTemplate restTemplate
    Logger logger

    def setup() {
        restTemplate = new RestTemplateBuilder().build()
        logger = LoggerFactory.getLogger(this.getClass())
    }

    def "assert addLoggingInterceptor is thread safe"() {
        when:
        AtomicBoolean concurrentModificationExceptionHit = new AtomicBoolean(false)
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
        // Tests showed that ConcurrentModificationException only happens reliably with about 200 threads
        int numberOfThreads = 200

        List threads = new ArrayList()
        for (int i = 0; i < numberOfThreads; i++) {
            def t = new Thread({
                try {
                    restTemplateBuilder.build()
                } catch (ConcurrentModificationException e) {
                    concurrentModificationExceptionHit.set(true)
                }
            })
            t.start()
            threads.add(t)
        }
        for (int i = 0; i < threads.size(); i++) {
            ((Thread) threads.get(i)).join()
        }
        then:
        !concurrentModificationExceptionHit.get()
        noExceptionThrown()
    }
}
