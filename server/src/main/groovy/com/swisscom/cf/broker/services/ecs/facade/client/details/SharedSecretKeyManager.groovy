package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod

class SharedSecretKeyManager {

    private final static String SHARED_SECRET_URL = "/object/user-secret-keys"

    @VisibleForTesting
    private RestTemplateReLoginDecorated<ECSMgmtUserPayload, ECSMgmtSharedSecretKeyPayload> restTemplateReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    ECSMgmtSharedSecretKeyResponse create(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload namespace) {
        return restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + SHARED_SECRET_URL + "/" + user.getUser(), HttpMethod.POST, namespace, ECSMgmtSharedSecretKeyResponse.class).getBody()
    }

    def delete(ECSMgmtUserPayload user, ECSMgmtSharedSecretKeyPayload sharedSecretKey) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + SHARED_SECRET_URL + "/" + user.getUser() + "/deactivate", HttpMethod.POST, sharedSecretKey, ECSMgmtSharedSecretKeyResponse.class)
    }

}
