package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator

class ECSManagementFacade {

    private ECSManagementInputDecorator ecsManagementInputFilter
    private ECSManagementClient ecsManagementClient


    def createNamespace(ECSMgmtNamespacePayload namespace) {
        ecsManagementInputFilter.decorate(namespace)
        ecsManagementClient.create(namespace)
    }

    def createUser(ECSMgmtUserPayload user) {
        ecsManagementInputFilter.decorate(user)
        ecsManagementClient.create(user)
    }

    def createBinding() {
        //return user, namespace, shared secret
    }


}
