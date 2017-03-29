package com.swisscom.cf.broker.services.ecs.facade.client

import com.swisscom.cf.broker.services.ecs.facade.client.details.BillingManager
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

    NamespaceManager namespaceManager
    UserManager userManager
    SharedSecretKeyManager sharedSecretKeyManager
    BillingManager billingManager

    def create(ECSMgmtNamespacePayload namespace) {
        namespaceManager.create(namespace)
    }

    def create(ECSMgmtUserPayload user) {
        userManager.create(user)
    }

    def delete(ECSMgmtUserPayload user) {
        userManager.delete(user)
    }

    def delete(ECSMgmtNamespacePayload namespace) {
        namespaceManager.delete(namespace)
    }

    def getUsage(ECSMgmtNamespacePayload namespace) {
        billingManager.getInformation(namespace)
    }

    ECSMgmtSharedSecretKeyResponse create(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload sharedSecretKeyPayload) {
        return sharedSecretKeyManager.create(user, sharedSecretKeyPayload)
    }
}
