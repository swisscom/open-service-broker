package com.swisscom.cf.broker.com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.BaseTransactionalSpecification
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.SharedSecretKeyManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyPayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtSharedSecretKeyResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class SharedSecretKeyManagerSpec extends BaseTransactionalSpecification {

    @Autowired
    ECSConfig ecsConfig

    def "add remove user shared secret"() {
        given:
        NamespaceManager namespaceManager = new NamespaceManager(ecsConfig)
        UserManager userManager = new UserManager(ecsConfig)
        SharedSecretKeyManager sharedSecretKeyManager = new SharedSecretKeyManager(ecsConfig)
        and:
        ECSMgmtNamespacePayload namespace = getNamespace()
        and:
        ECSMgmtUserPayload ecsMgmtUserPayload = new ECSMgmtUserPayload()
        ecsMgmtUserPayload.namespace = ecsConfig.getEcsManagementNamespacePrefix() + "675c663317e943579636e88e30"
        ecsMgmtUserPayload.user = ecsConfig.getEcsManagementNamespacePrefix() + "675c663317e943579636e88e30-user4"
        and:
        ECSMgmtSharedSecretKeyPayload ecsMgmtSharedSecretKeyPayload = new ECSMgmtSharedSecretKeyPayload(namespace: ecsMgmtUserPayload.namespace)
        when:
        namespaceManager.create(namespace)
        userManager.create(ecsMgmtUserPayload)
        ECSMgmtSharedSecretKeyResponse response = sharedSecretKeyManager.create(ecsMgmtUserPayload, ecsMgmtSharedSecretKeyPayload)
        then:
        response.getSecret_key().isEmpty() == false
        cleanup:
        userManager.delete(ecsMgmtUserPayload)
        namespaceManager.delete(namespace)
        sharedSecretKeyManager.restTemplateReLoginDecorated.logout(ecsConfig.getEcsManagementBaseUrl())

    }

    ECSMgmtNamespacePayload getNamespace() {
        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = ecsConfig.getEcsManagementNamespacePrefix() + "675c663317e943579636e88e30"
        namespace.default_data_services_vpool = ecsConfig.getEcsDefaultDataServicesVpool()
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false
        namespace
    }

}
