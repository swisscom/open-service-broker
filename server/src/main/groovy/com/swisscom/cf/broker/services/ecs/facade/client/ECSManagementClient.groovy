package com.swisscom.cf.broker.services.ecs.facade.client

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.commands.CreateNamespace
import com.swisscom.cf.broker.services.ecs.facade.client.commands.CreateUser

class ECSManagementClient {

    CreateNamespace createNamespace
    CreateUser createUser

    def create(Namespace namespace) {
        createNamespace.create(namespace)
    }

    def create(User user) {
        createUser.create(user)
    }
}
