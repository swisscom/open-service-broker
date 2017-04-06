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

    def "add namespace"() {
        given:
        NamespaceManager namespaceManager = new NamespaceManager(ecsConfig)
        and:
        ECSMgmtNamespacePayload namespace = getNamespace()
        when:
        ResponseEntity response = namespaceManager.create(namespace)
        then:
        response.statusCode == HttpStatus.OK
        cleanup:
        namespaceManager.delete(namespace)
    }

    def "remove namespace"() {
        given:
        NamespaceManager namespaceManager = new NamespaceManager(ecsConfig)
        and:
        ECSMgmtNamespacePayload namespace = getNamespace()
        when:
        namespaceManager.create(namespace)
        ResponseEntity response = namespaceManager.delete(namespace)
        then:
        response.statusCode == HttpStatus.OK
    }

    ECSMgmtNamespacePayload getNamespace() {
        ECSMgmtNamespacePayload namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = ecsConfig.getEcsManagementNamespacePrefix() + "481612728ec51007f46e58c743"
        namespace.default_data_services_vpool = ecsConfig.getEcsDefaultDataServicesVpool()
        namespace.is_encryption_enabled = false
        namespace.default_bucket_block_size = -1
        namespace.is_stale_allowed = true
        namespace.compliance_enabled = false
        namespace
    }

}
