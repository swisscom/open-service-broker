package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.SharedSecretKey
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespaceResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.SharedSecretKeyToECSMgmtSharedSecretKey
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.UserToECSMgmtUser
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class SharedSecretKeyManager {

    private final static String SHARED_SECRET_URL = "/object/user-secret-keys"

    @VisibleForTesting
    private RestTemplateFactoryReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtNamespaceResponse> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private SharedSecretKeyToECSMgmtSharedSecretKey sharedSecretKeyToECSMgmtSharedSecretKey
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(User user, SharedSecretKey sharedSecretKey) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + SHARED_SECRET_URL + "/" + user.getUser(), HttpMethod.POST, sharedSecretKeyToECSMgmtSharedSecretKey.adapt(sharedSecretKey), ECSMgmtSharedSecretKeyResponse.class)
    }

    def delete(User user, SharedSecretKey sharedSecretKey) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + SHARED_SECRET_URL + "/" + user.getUser() + "/deactivate", HttpMethod.POST, sharedSecretKeyToECSMgmtSharedSecretKey.adapt(sharedSecretKey), ECSMgmtSharedSecretKeyResponse.class)
    }

}
