package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class UserManagerSpec extends Specification {

    UserManager userManager
    RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated
    ECSMgmtUserPayload user
    ECSConfig ecsConfig

    def setup() {
        user = new ECSMgmtUserPayload()
        restTemplateFactoryReLoginDecorated = Mock()
        ecsConfig = Stub()
        userManager = new UserManager(ecsConfig)
        userManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecorated
    }

    def "create User call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        userManager.create(user)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/users", HttpMethod.POST, _, _)
    }

    def "delete User call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        userManager.delete(user)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/users/deactivate", HttpMethod.POST, _, _)
    }

    def "is exists return false when User not exists"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        responseEntity.getStatusCode() >> HttpStatus.BAD_REQUEST
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/users/idUser", HttpMethod.GET, _, _) >> responseEntity
        userManager = new UserManager(ecsConfig)
        userManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecoratedStubbed
        then:
        false == userManager.isExists(user)
    }

    def "is exists return true when User exists"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/Users/idUser", HttpMethod.GET, null, ECSMgmtUserResponse.class) >> responseEntity
        userManager = new UserManager(ecsConfig)
        userManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecoratedStubbed
        then:
        true == userManager.isExists(user)
    }


}
