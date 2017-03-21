package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.UserToECSMgmtUser
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class UserManagerSpec extends Specification {

    UserManager userManager
    RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecorated
    UserToECSMgmtUser userToECSMgmtUser
    User User
    ECSConfig ecsConfig

    def setup() {
        User = new User()
        restTemplateFactoryReLoginDecorated = Mock()
        userToECSMgmtUser = Stub()
        ecsConfig = Stub()
        userManager = new UserManager(userToECSMgmtUser: userToECSMgmtUser, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated)
    }

    def "create User call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        User.user = "idUser"
        userManager.create(User)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/users", HttpMethod.POST, _, ECSMgmtUserResponse.class)
    }

    def "delete User call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        User.user = "idUser"
        userManager.delete(User)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/users/idUser/deactivate", HttpMethod.POST, _, ECSMgmtUserResponse.class)
    }

    def "is exists return false when User not exists"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        User.user = "idUser"
        RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        responseEntity.getStatusCode() >> HttpStatus.BAD_REQUEST
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/users/idUser", HttpMethod.GET, null, ECSMgmtUserResponse.class) >> responseEntity
        userManager = new UserManager(userToECSMgmtUser: userToECSMgmtUser, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecoratedStubbed)
        then:
        false == userManager.isExists(User)
    }

    def "is exists return true when User exists"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        User.user = "idUser"
        RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/Users/idUser", HttpMethod.GET, null, ECSMgmtUserResponse.class) >> responseEntity
        userManager = new UserManager(userToECSMgmtUser: userToECSMgmtUser, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecoratedStubbed)
        then:
        true == userManager.isExists(User)
    }


}
