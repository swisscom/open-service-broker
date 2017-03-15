package com.swisscom.cf.broker.services.ecs.facade.client.exception

import com.swisscom.cf.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus


class ECSManagementAuthenticationException extends ServiceBrokerException {

    ECSManagementAuthenticationException() {
        super("Unable to login to ECS management service", null, null, HttpStatus.UNAUTHORIZED)
    }
}
