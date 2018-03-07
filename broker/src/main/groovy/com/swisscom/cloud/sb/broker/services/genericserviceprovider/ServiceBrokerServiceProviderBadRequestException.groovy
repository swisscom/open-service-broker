package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus

class ServiceBrokerServiceProviderBadRequestException extends ServiceBrokerException{
    ServiceBrokerServiceProviderBadRequestException(String description, String code, String error_code, HttpStatus httpStatus) {
        super(description, code, error_code, httpStatus)
    }
}
