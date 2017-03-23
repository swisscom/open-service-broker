package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.*
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod

class SharedSecretKeyManager {

    private final static String SHARED_SECRET_URL = "/object/user-secret-keys"

    @VisibleForTesting
    private RestTemplateReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtNamespaceResponse> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload sharedSecretKey) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + SHARED_SECRET_URL + "/" + user.getUser(), HttpMethod.POST, sharedSecretKey, ECSMgmtSharedSecretKeyResponse.class)
    }

    def delete(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload sharedSecretKey) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + SHARED_SECRET_URL + "/" + user.getUser() + "/deactivate", HttpMethod.POST, sharedSecretKey, ECSMgmtSharedSecretKeyResponse.class)
    }

}
