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

import groovy.util.logging.Slf4j
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.StopWatch

@Slf4j
class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        ClientHttpResponse response = execution.execute(request, body)
        stopWatch.stop()
        log(request, response, stopWatch.getTotalTimeMillis())
        response
    }

    private void log(HttpRequest request, ClientHttpResponse response, long durationInMS) throws IOException {
        log.info("Request: ${request.getMethod()} ${request.getURI()} - Duration: ${durationInMS}ms - Response: ${response.getStatusCode()}")
    }
}
