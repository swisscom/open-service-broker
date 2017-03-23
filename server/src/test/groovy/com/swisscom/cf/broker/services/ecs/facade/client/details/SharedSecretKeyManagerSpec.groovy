package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod
import spock.lang.Specification

class SharedSecretKeyManagerSpec extends Specification {

    SharedSecretKeyManager sharedSecretKeyManager
    RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated
    ECSMgmtUserPayload user
    ECSMgmtSharedSecretKeyPayload sharedSecretKey
    ECSConfig ecsConfig

    def setup() {
        user = new ECSMgmtUserPayload()
        sharedSecretKey = new ECSMgmtSharedSecretKeyPayload()
        restTemplateFactoryReLoginDecorated = Mock()
        ecsConfig = Stub()
        sharedSecretKeyManager = new SharedSecretKeyManager(ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated)
    }

    def "create sharedSecretKey call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        sharedSecretKeyManager.create(user, sharedSecretKey)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/user-secret-keys/idUser", HttpMethod.POST, _, _)
    }

    def "delete sharedSecretKey call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        sharedSecretKeyManager.delete(user, sharedSecretKey)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/user-secret-keys/idUser/deactivate", HttpMethod.POST, _, _)
    }


}
