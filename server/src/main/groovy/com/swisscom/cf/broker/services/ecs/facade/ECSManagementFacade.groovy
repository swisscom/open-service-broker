package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.model.DeprovisionRequest
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.provisioning.DeprovisionResponse
import com.swisscom.cf.broker.provisioning.ProvisionResponse
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator
import com.swisscom.cf.broker.util.ServiceDetailKey
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.transform.CompileStatic

import static com.swisscom.cf.broker.model.ServiceDetail.from

class ECSManagementFacade {

    private ECSManagementInputDecorator ecsManagementInputFilter
    private ECSManagementClient ecsManagementClient

    ECSManagementFacade(ECSManagementInputDecorator ecsManagementInputFilter, ECSManagementClient ecsManagementClient) {
        this.ecsManagementInputFilter = ecsManagementInputFilter
        this.ecsManagementClient = ecsManagementClient
    }


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

    DeprovisionResponse deprovision(DeprovisionRequest request) {
        ecsManagementClient.delete(new ECSMgmtUserPayload(
                user: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.ECS_NAMESPACE_USER),
                namespace: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.ECS_NAMESPACE_NAME)))
        ecsManagementClient.delete(new ECSMgmtNamespacePayload(
                namespace: ServiceDetailsHelper.from(request.serviceInstance.details).getValue(ServiceDetailKey.ECS_NAMESPACE_NAME)))
        return new DeprovisionResponse(isAsync: false)
    }

    String getUsageInformation(ECSMgmtNamespacePayload ecsMgmtNamespacePayload) {
        return (new BigDecimal(ecsManagementClient.getUsage(ecsMgmtNamespacePayload).total_size)).multiply(new BigDecimal("1024")).toPlainString()
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

}
