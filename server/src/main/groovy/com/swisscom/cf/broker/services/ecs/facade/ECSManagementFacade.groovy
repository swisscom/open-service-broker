package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputFilter
import com.swisscom.cf.broker.services.ecs.facade.validators.ECSManagementInputValidator

class ECSManagementFacade {

    private ECSManagementInputValidator ecsManagementInputValidator
    private ECSManagementInputFilter ecsManagementInputFilter
    private ECSManagementClient ecsManagementClient


    def createNamespace(Namespace namespace) {
        ecsManagementInputFilter.filter(namespace)
        ecsManagementInputValidator.validiate(namespace)
        ecsManagementClient.create(namespace)

    }

    def createUser(User user) {
        ecsManagementInputFilter.filter(user)
        ecsManagementInputValidator.validiate(user)
        ecsManagementClient.create(user)
    }


}
