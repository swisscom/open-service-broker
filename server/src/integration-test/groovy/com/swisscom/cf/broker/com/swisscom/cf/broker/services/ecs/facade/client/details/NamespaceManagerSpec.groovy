package com.swisscom.cf.broker.com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.BaseTransactionalSpecification
import com.swisscom.cf.broker.model.LastOperation
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContext
import com.swisscom.cf.broker.provisioning.lastoperation.LastOperationJobContextService
import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.TokenManager
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import com.swisscom.cf.broker.util.DBTestUtil
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class NamespaceManagerSpec extends BaseTransactionalSpecification {

    @Autowired
    ECSConfig ecsConfig

    def "list namespaces"() {
        given:
        NamespaceManager namespaceManager = new NamespaceManager(ecsConfig)
        when:
        ResponseEntity response = namespaceManager.list()
        then:
        response.statusCode == HttpStatus.OK

    }

    def "add remove namespace"() {
        given:
        NamespaceManager namespaceManager = new NamespaceManager(ecsConfig)

        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = "8094bd675c663317e943579636e88e30"
        namespace.default_data_services_vpool = "urn:storageos:ReplicationGroupInfo:e4cf1d55-7f6f-4e64-be95-52c87d3b465d:global"
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false

        when:
        ResponseEntity response = namespaceManager.create(namespace)
        namespaceManager.delete(namespace)
        then:
        response.statusCode == HttpStatus.OK

    }

}
