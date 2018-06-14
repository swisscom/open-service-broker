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

import org.springframework.http.HttpMethod
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class LoggingRequestInterceptorSpec extends Specification {
    RestTemplate restTemplate
    TestLogger logger
    String testURL = "https://developer.swisscom.com"

    def setup() {
        restTemplate = new RestTemplateBuilder().build()
        logger = TestLoggerFactory.getTestLogger(LoggingRequestInterceptor.class)
    }

    def "happy path: check if RestTemplate GET request is logged"() {
        when:
            restTemplate.exchange(testURL, HttpMethod.GET, null, String.class)

        then:
            LoggingEvent logEvent = logger.getLoggingEvents().asList()[0]
            assert logEvent.getLevel() == Level.INFO
            assert logEvent.getMessage() =~ /Request: GET ${testURL} - Duration: [0-9]+ms - Response: 200/
            noExceptionThrown()
    }

    def "error case: check if RestTemplate POST request to not existing endpoint is logged"() {
        when:
            try {
                restTemplate.exchange(testURL, HttpMethod.POST, null, String.class)
            } catch (HttpClientErrorException e) {
                if (e.getMessage() != "404 Not Found") throw e
            }

        then:
            LoggingEvent logEvent = logger.getLoggingEvents().asList()[1]
            assert logEvent.getLevel() == Level.INFO
            assert logEvent.getMessage() =~ /Request: POST ${testURL} - Duration: [0-9]+ms - Response: 404/
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
