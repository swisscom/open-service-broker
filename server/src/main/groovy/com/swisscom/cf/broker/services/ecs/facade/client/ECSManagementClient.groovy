package com.swisscom.cf.broker.services.ecs.facade.client

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager

class ECSManagementClient {

    NamespaceManager createNamespace
    UserManager createUser

    def create(Namespace namespace) {
        createNamespace.create(namespace)
    }

    def create(User user) {
        createUser.create(user)
    }
}
