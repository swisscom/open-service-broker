package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespaceResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.NamespaceToECSMgmtNamespace
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@CompileStatic
class NamespaceManager {

    private final static String NAMESPACE_URL = "/object/namespaces/namespace"
    private final static String NAMESPACE_LIST_URL = "/object/namespaces"

    @VisibleForTesting
    private RestTemplateFactoryReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtNamespaceResponse> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private NamespaceToECSMgmtNamespace namespaceToECSMgmtNamespace
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(Namespace namespace) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL, HttpMethod.POST, namespaceToECSMgmtNamespace.adapt(namespace), ECSMgmtNamespaceResponse.class)
    }

    def list() {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_LIST_URL, HttpMethod.GET, null, ECSMgmtNamespaceResponse.class)
    }

    def delete(Namespace namespace) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL + "/" + namespace.getNamespace() + "/deactivate", HttpMethod.POST, null, ECSMgmtNamespaceResponse.class)
    }

    def isExists(Namespace namespace) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL + "/" + namespace.getNamespace(), HttpMethod.GET, null, ECSMgmtNamespaceResponse.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }


}
