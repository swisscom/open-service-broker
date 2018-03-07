package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus

class ServiceBrokerServiceProviderServiceNotFoundException extends ServiceBrokerException {
    ServiceBrokerServiceProviderServiceNotFoundException(String description, String code, String error_code, HttpStatus httpStatus) {
        super(description, code, error_code, httpStatus)
    }

}
