package com.swisscom.cloud.sb.broker.services.bosh.client

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus


class BoshResourceNotFoundException extends ServiceBrokerException {

    BoshResourceNotFoundException(String description, String code, String error_code, HttpStatus httpStatus) {
        super(description, code, error_code, httpStatus)
    }
}
