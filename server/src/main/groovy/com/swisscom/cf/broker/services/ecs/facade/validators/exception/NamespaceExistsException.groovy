package com.swisscom.cf.broker.services.ecs.facade.validators.exception

import com.swisscom.cf.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus


class NamespaceExistsException extends ServiceBrokerException {

    NamespaceExistsException() {
        super("Namespace already exists", null, null, HttpStatus.IM_USED)
    }
}
