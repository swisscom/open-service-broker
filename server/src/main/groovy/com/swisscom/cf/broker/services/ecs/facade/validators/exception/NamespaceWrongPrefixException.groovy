package com.swisscom.cf.broker.services.ecs.facade.validators.exception

import com.swisscom.cf.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus


class NamespaceWrongPrefixException extends ServiceBrokerException {

    NamespaceWrongPrefixException() {
        super("Namespace has wrong prefix", null, null, HttpStatus.BAD_REQUEST)
    }
}
