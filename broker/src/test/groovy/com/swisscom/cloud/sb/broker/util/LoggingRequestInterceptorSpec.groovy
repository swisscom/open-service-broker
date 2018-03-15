package com.swisscom.cloud.sb.broker.util

import org.springframework.http.HttpMethod
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.LoggingEvent
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

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
}
