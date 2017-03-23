package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        restTemplateFactoryReLoginDecorated = Stub()
        ecsConfig = Stub()
        sharedSecretKeyManager = new SharedSecretKeyManager(ecsConfig: ecsConfig, restTemplateReLoginDecorated: restTemplateFactoryReLoginDecorated)
    }

    def "create sharedSecretKey call proper endpoint"() {
        when:
        ECSMgmtSharedSecretKeyResponse ecsMgmtSharedSecretKeyResponse = new ECSMgmtSharedSecretKeyResponse()
        ResponseEntity result = new ResponseEntity(ecsMgmtSharedSecretKeyResponse, HttpStatus.ACCEPTED)

        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"

        restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/user-secret-keys/idUser", HttpMethod.POST, _, _) >> result
        then:
        sharedSecretKeyManager.create(user, sharedSecretKey) == ecsMgmtSharedSecretKeyResponse
    }

    def "delete sharedSecretKey call proper endpoint"() {
        when:
        ResponseEntity result = new ResponseEntity(HttpStatus.ACCEPTED)
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        user.user = "idUser"
        restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/user-secret-keys/idUser/deactivate", HttpMethod.POST, _, _) >> result
        then:
        sharedSecretKeyManager.delete(user, sharedSecretKey).getStatusCode() == HttpStatus.ACCEPTED
    }


}
