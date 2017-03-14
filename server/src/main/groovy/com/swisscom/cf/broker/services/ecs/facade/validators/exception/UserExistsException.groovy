package com.swisscom.cf.broker.services.ecs.facade.validators.exception

import com.swisscom.cf.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus


class UserExistsException extends ServiceBrokerException {

    UserExistsException(String description, String code, String error_code, HttpStatus httpStatus) {
        super(description, code, error_code, httpStatus)
    }
}
