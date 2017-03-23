package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class UserManager {

    private final static String USER_URL = "/object/users"

    @VisibleForTesting
    private RestTemplateReLoginDecorated<ECSMgmtUserPayload, String> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(ECSMgmtUserPayload user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL, HttpMethod.POST, user, String.class)
    }

    def delete(ECSMgmtUserPayload user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/" + user.getUser() + "/deactivate", HttpMethod.POST, user, String.class)
    }

    def isExists(ECSMgmtUserPayload user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/" + user.getUser(), HttpMethod.GET, null, String.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }
}
