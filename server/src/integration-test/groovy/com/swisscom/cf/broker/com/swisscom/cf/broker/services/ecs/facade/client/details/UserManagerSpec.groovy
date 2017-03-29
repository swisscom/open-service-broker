package com.swisscom.cf.broker.com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.BaseTransactionalSpecification
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class UserManagerSpec extends BaseTransactionalSpecification {

    @Autowired
    ECSConfig ecsConfig

    def "add remove user"() {
        given:
        NamespaceManager namespaceManager = new NamespaceManager(ecsConfig)
        UserManager userManager = new UserManager(ecsConfig)
        and:
        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = "8094bd675c663317e943579636e88e30"
        namespace.default_data_services_vpool = "urn:storageos:ReplicationGroupInfo:e4cf1d55-7f6f-4e64-be95-52c87d3b465d:global"
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false
        and:
        ECSMgmtUserPayload ecsMgmtUserPayload = new ECSMgmtUserPayload()
        ecsMgmtUserPayload.namespace = "8094bd675c663317e943579636e88e30"
        ecsMgmtUserPayload.user = "8094bd675c663317e943579636e88e30-user4"
        when:
        namespaceManager.create(namespace)
        userManager.create(ecsMgmtUserPayload)
        ResponseEntity response = userManager.delete(ecsMgmtUserPayload)
        then:
        response.statusCode == HttpStatus.OK
        cleanup:
        namespaceManager.delete(namespace)

    }

}
