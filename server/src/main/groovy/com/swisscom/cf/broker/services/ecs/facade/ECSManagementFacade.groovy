package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.provisioning.ProvisionResponse
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator
import com.swisscom.cf.broker.util.ServiceDetailKey
import static com.swisscom.cf.broker.model.ServiceDetail.from
import groovy.transform.CompileStatic

@CompileStatic
class ECSManagementFacade {

    private ECSManagementInputDecorator ecsManagementInputFilter
    private ECSManagementClient ecsManagementClient


    ProvisionResponse provision() {
        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        createNamspace(namespace)
        ECSMgmtUserPayload user = new ECSMgmtUserPayload(namespace: namespace.namespace, user: namespace.namespace)
        createUser(user)
        ECSMgmtSharedSecretKeyPayload ecsMgmtSharedSecretKeyPayload = new ECSMgmtSharedSecretKeyPayload(namespace: namespace.namespace)
        ECSMgmtSharedSecretKeyResponse ecsMgmtSharedSecretKeyResponse = createECSMgmtSharedSecretKey(user, ecsMgmtSharedSecretKeyPayload)
        return new ProvisionResponse(details: [from(ServiceDetailKey.ECS_NAMESPACE_NAME, namespace.namespace),
                                               from(ServiceDetailKey.ECS_NAMESPACE_USER, user.user),
                                               from(ServiceDetailKey.ECS_NAMESPACE_SECRET, ecsMgmtSharedSecretKeyResponse.secret_key)])
    }

    def createNamspace(ECSMgmtNamespacePayload namespace) {
        ecsManagementInputFilter.decorate(namespace)
        ecsManagementClient.create(namespace)
    }

    def createUser(ECSMgmtUserPayload user) {
        ecsManagementInputFilter.decorate(user)
        ecsManagementClient.create(user)
    }

    ECSMgmtSharedSecretKeyResponse createECSMgmtSharedSecretKey(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload ecsMgmtSharedSecretKeyPayload) {
        return ecsManagementClient.create(user, ecsMgmtSharedSecretKeyPayload)
    }

    def createBinding() {
        //return user, namespace, shared secret
    }


}
