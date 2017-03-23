package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@CompileStatic
class NamespaceManager {

    private final static String NAMESPACE_URL = "/object/namespaces/namespace"
    private final static String NAMESPACE_LIST_URL = "/object/namespaces"

    @VisibleForTesting
    private RestTemplateFactoryReLoginDecorated<ECSMgmtNamespacePayload, String> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(ECSMgmtNamespacePayload namespace) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL, HttpMethod.POST, namespace, String.class)
    }

    def list() {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_LIST_URL, HttpMethod.GET, null, String.class)
    }

    def delete(ECSMgmtNamespacePayload namespace) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL + "/" + namespace.getNamespace() + "/deactivate", HttpMethod.POST, null, String.class)
    }

    def isExists(ECSMgmtNamespacePayload namespace) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL + "/" + namespace.getNamespace(), HttpMethod.GET, null, String.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }


}
