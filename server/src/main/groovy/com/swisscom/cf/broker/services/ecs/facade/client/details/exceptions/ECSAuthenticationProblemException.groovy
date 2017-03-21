package com.swisscom.cf.broker.services.ecs.facade.client.details.exceptions

import com.swisscom.cf.broker.error.ServiceBrokerException
import org.springframework.http.HttpStatus


class ECSAuthenticationProblemException extends ServiceBrokerException {

    ECSAuthenticationProblemException() {
        super("There is an issue with ECS auth - please invastigate", null, null, HttpStatus.UNAUTHORIZED)
    }
}
