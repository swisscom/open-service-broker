package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class ShieldRestResponseErrorHandler implements ResponseErrorHandler {

    @Override
    void handleError(ClientHttpResponse response) throws IOException{
        String errorMessage = "Rest call to Shield failed with status:${response.statusCode}"
        // Shield raises a 501 and not a 404 if you query GET /v1/task/null or /v1/task/; let's handle this as it were a 404
        if (HttpStatus.NOT_IMPLEMENTED == response.statusCode) {
            throw new ShieldResourceNotFoundException(errorMessage)
        } else {
            throw new ServiceBrokerException(errorMessage)
        }
    }

    @Override
    boolean hasError(ClientHttpResponse response) throws IOException {
        return !response.statusCode.'2xxSuccessful'
    }
}
