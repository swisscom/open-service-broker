package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator

class ECSManagementFacade {

    private ECSManagementInputDecorator ecsManagementInputFilter
    private ECSManagementClient ecsManagementClient


    def createNamespace(Namespace namespace) {
        ecsManagementInputFilter.decorate(namespace)
        ecsManagementClient.create(namespace)
    }

    def createUser(User user) {
        ecsManagementInputFilter.decorate(user)
        ecsManagementClient.create(user)
    }

    def createBinding() {
        //return user, namespace, shared secret
    }


}
