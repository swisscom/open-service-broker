package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class UserManager {

    private final static String USER_URL = "/object/users"

    @VisibleForTesting
    private RestTemplateReLoginDecorated<ECSMgmtUserPayload, ECSMgmtUserResponse> restTemplateReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    UserManager(ECSConfig ecsConfig) {
        this.ecsConfig = ecsConfig
        this.restTemplateReLoginDecorated = new RestTemplateReLoginDecorated<>(new TokenManager(ecsConfig, new RestTemplateFactory()))
    }

    def create(ECSMgmtUserPayload user) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL, HttpMethod.POST, user, ECSMgmtUserResponse.class)
    }

    def delete(ECSMgmtUserPayload user) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/deactivate", HttpMethod.POST, user, ECSMgmtUserResponse.class)
    }

    def isExists(ECSMgmtUserPayload user) {
        restTemplateReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/" + user.getUser(), HttpMethod.GET, user, ECSMgmtUserResponse.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }
}
