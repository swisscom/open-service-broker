package com.swisscom.cf.broker.services.ecs.facade.client

import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.SharedSecretKeyManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import groovy.transform.CompileStatic

@CompileStatic
class ECSManagementClient {

    NamespaceManager createNamespace
    UserManager createUser
    SharedSecretKeyManager sharedSecretKeyManager

    def create(ECSMgmtNamespacePayload namespace) {
        createNamespace.create(namespace)
    }

    def create(ECSMgmtUserPayload user) {
        createUser.create(user)
    }

    ECSMgmtSharedSecretKeyResponse create(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload sharedSecretKeyPayload) {
        return sharedSecretKeyManager.create(user, sharedSecretKeyPayload)
    }
}
