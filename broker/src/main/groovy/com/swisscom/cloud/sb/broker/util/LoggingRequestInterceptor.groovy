package com.swisscom.cloud.sb.broker.util

import groovy.util.logging.Slf4j
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

@Slf4j
class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body)
        log(request, response)
        response
    }

    private void log(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.info("Request: ${request.getMethod()} ${request.getURI()} - Response: ${response.getStatusCode()}")
    }
}
