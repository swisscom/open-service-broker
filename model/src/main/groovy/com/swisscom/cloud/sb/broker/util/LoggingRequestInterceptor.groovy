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
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StopWatch
import org.springframework.util.StreamUtils

import java.nio.charset.Charset

class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRequestInterceptor.class)

    @Override
    ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws
            IOException {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        ClientHttpResponse response = execution.execute(request, body)
        stopWatch.stop()
        log(request, body, response, stopWatch.getTotalTimeMillis())
        response
    }

    private static void log(HttpRequest request, byte[] body, ClientHttpResponse response, long durationInMS) throws
            IOException {
        LOGGER.info("Request: {} {} - Duration: {}ms - Response: {}",
                     request.getMethod(),
                     request.getURI(),
                     durationInMS,
                     response.getStatusCode())
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request Header: {}", request.getHeaders())
            LOGGER.debug("Request Body: {}", body ? new String(body, "UTF-8") : "")
            LOGGER.debug("Response Header: {}", response.getHeaders())
            LOGGER.debug("Response Body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()))
        }
    }
}
