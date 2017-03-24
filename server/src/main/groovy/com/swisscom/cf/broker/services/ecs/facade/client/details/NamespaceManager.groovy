package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import groovy.transform.CompileStatic
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@CompileStatic
class NamespaceManager {

    private final static String NAMESPACE_URL = "/object/namespaces/namespace"
    private final static String NAMESPACE_LIST_URL = "/object/namespaces"

    @VisibleForTesting
    private RestTemplateReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtSharedSecretKeyResponse> restTemplateReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    NamespaceManager(ECSConfig ecsConfig) {
        this.ecsConfig = ecsConfig
        this.restTemplateReLoginDecorated = new RestTemplateReLoginDecorated<>(new TokenManager(ecsConfig, new RestTemplateFactory()))
    }

    def create(ECSMgmtNamespacePayload namespace) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL, HttpMethod.POST, namespace, ECSMgmtSharedSecretKeyResponse.class)
    }

    def list() {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_LIST_URL, HttpMethod.GET, null, ECSMgmtSharedSecretKeyResponse.class)
    }

    def delete(ECSMgmtNamespacePayload namespace) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL + "/" + namespace.getNamespace() + "/deactivate", HttpMethod.POST, null, ECSMgmtSharedSecretKeyResponse.class)
    }

    def isExists(ECSMgmtNamespacePayload namespace) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + NAMESPACE_URL + "/" + namespace.getNamespace(), HttpMethod.GET, null, ECSMgmtSharedSecretKeyResponse.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }


}
