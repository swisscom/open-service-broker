package com.swisscom.cloud.sb.broker.error

import groovy.transform.CompileStatic
import org.springframework.http.HttpStatus

@CompileStatic
class ServiceBrokerException extends RuntimeException {
    String description
    String code
    String error_code
    HttpStatus httpStatus

    ServiceBrokerException(String description, String code, String error_code, HttpStatus httpStatus) {
        super(description)
        this.description = description
        this.code = code
        this.error_code = error_code
        this.httpStatus = httpStatus
    }

    ServiceBrokerException(String description) {
        this(description, null, null, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
