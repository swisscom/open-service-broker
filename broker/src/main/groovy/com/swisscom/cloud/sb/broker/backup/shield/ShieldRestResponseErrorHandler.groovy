package com.swisscom.cloud.sb.broker.backup.shield

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

class ShieldRestResponseErrorHandler implements ResponseErrorHandler {

    @Override
    void handleError(ClientHttpResponse response) throws IOException{
        String errorMessage = "Rest call to Shield failed with status:${response.statusCode}"
        if (HttpStatus.NOT_FOUND == response.statusCode) {
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
