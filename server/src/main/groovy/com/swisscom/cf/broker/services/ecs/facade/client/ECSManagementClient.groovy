package com.swisscom.cf.broker.services.ecs.facade.client

import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload

class ECSManagementClient {

    NamespaceManager createNamespace
    UserManager createUser

    def create(ECSMgmtNamespacePayload namespace) {
        createNamespace.create(namespace)
    }

    def create(ECSMgmtUserPayload user) {
        createUser.create(user)
    }
}
