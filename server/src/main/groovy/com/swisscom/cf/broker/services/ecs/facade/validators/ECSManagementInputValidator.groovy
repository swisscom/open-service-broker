package com.swisscom.cf.broker.services.ecs.facade.validators

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.NamespaceExistsException
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.NamespaceWrongPrefixException
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.UserExistsException

class ECSManagementInputValidator {

    NamespaceManager namespaceManager
    UserManager userManager
    ECSConfig ecsConfig

    def validate(Namespace namespace) {
        if (namespaceManager.isExists(namespace)) {
            throw new NamespaceExistsException()
        }
        if (!namespace.getNamespace().startsWith(ecsConfig.getEcsManagementNamespacePrefix())) {
            throw new NamespaceWrongPrefixException()
        }
    }

    def validate(User user) {
        if (userManager.isExists(user)) {
            throw new UserExistsException()
        }
    }


}

