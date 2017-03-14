package com.swisscom.cf.broker.services.ecs.facade.validators

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.NamespaceExistsException
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.UserExistsException
import org.springframework.http.HttpStatus


class ECSManagementInputValidator {

    def validiate(Namespace namespace) {
        if (isNamespaceExists(namespace)) {
            throw new NamespaceExistsException("Namespace already exists", null, null, HttpStatus.IM_USED)
        }
    }

    def isNamespaceExists(Namespace namespace) {

    }

    def validiate(User user) {
        if (isUserExists(user)) {
            throw new UserExistsException("User already exists", null, null, HttpStatus.IM_USED)
        }
    }

    def isUserExists(User user) {

    }

}

