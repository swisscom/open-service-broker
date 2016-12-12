package com.swisscom.cf.broker.services.bosh.client

import com.swisscom.cf.broker.exception.ServiceBrokerException


class BoshResourceNotFoundException extends ServiceBrokerException {

    BoshResourceNotFoundException(String description, String code, String error_code, int httpStatus) {
        super(description, code, error_code, httpStatus)
    }
}
