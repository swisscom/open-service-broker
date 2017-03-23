package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class UserManager {

    private final static String USER_URL = "/object/users"

    @VisibleForTesting
    private RestTemplateFactoryReLoginDecorated<ECSMgmtUserPayload, ECSMgmtUserResponse> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(ECSMgmtUserPayload user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL, HttpMethod.POST, user, ECSMgmtUserResponse.class)
    }

    def delete(ECSMgmtUserPayload user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/deactivate", HttpMethod.POST, user, ECSMgmtUserResponse.class)
    }

    def isExists(ECSMgmtUserPayload user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/" + user.getUser(), HttpMethod.GET, null, ECSMgmtUserResponse.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }
}
