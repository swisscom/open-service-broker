package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.google.common.annotations.VisibleForTesting
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespaceResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.UserToECSMgmtUser
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class UserManager {

    private final static String USER_URL = "/object/users"

    @VisibleForTesting
    private RestTemplateFactoryReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtNamespaceResponse> restTemplateFactoryReLoginDecorated
    @VisibleForTesting
    private UserToECSMgmtUser userToECSMgmtUser
    @VisibleForTesting
    private ECSConfig ecsConfig

    def create(User user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL, HttpMethod.POST, userToECSMgmtUser.adapt(user), ECSMgmtUserResponse.class)
    }

    def delete(User user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/" + user.getUser() + "/deactivate", HttpMethod.POST, userToECSMgmtUser.adapt(user), ECSMgmtUserResponse.class)
    }

    def isExists(User user) {
        restTemplateFactoryReLoginDecorated.exchange(ecsConfig.getEcsManagementBaseUrl() + USER_URL + "/" + user.getUser(), HttpMethod.GET, null, ECSMgmtUserResponse.class).getStatusCode() != HttpStatus.BAD_REQUEST
    }
}
