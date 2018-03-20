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
