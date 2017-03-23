package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.SharedSecretKeyManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import com.swisscom.cf.broker.util.ServiceLifeCycler
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ECSSecretManagerFunctionalSpec extends BaseFunctionalSpec {


    @Override
    void init(ServiceLifeCycler serviceLifeCycler) {
        super.init(serviceLifeCycler)
    }


}