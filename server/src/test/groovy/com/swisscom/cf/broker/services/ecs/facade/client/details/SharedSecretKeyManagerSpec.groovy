package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.SharedSecretKey
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.SharedSecretKeyToECSMgmtSharedSecretKey
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.UserToECSMgmtUser
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class SharedSecretKeyManagerSpec extends Specification {

    SharedSecretKeyManager sharedSecretKeyManager
    RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecorated
    SharedSecretKeyToECSMgmtSharedSecretKey sharedSecretKeyToECSMgmtSharedSecretKey
    User user
    SharedSecretKey sharedSecretKey
    ECSConfig ecsConfig

    def setup() {
        user = new User()
        sharedSecretKey = new SharedSecretKey()
        restTemplateFactoryReLoginDecorated = Mock()
        sharedSecretKeyToECSMgmtSharedSecretKey = Stub()
        ecsConfig = Stub()
        sharedSecretKeyManager = new SharedSecretKeyManager(sharedSecretKeyToECSMgmtSharedSecretKey: sharedSecretKeyToECSMgmtSharedSecretKey, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated)
    }

    def "create sharedSecretKey call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        sharedSecretKeyManager.create(user, sharedSecretKey)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/user-secret-keys/idUser", HttpMethod.POST, _, ECSMgmtSharedSecretKeyResponse.class)
    }

    def "delete sharedSecretKey call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        sharedSecretKeyManager.delete(user, sharedSecretKey)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/user-secret-keys/idUser/deactivate", HttpMethod.POST, _, ECSMgmtSharedSecretKeyResponse.class)
    }


}
