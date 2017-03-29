package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification


class NamespaceManagerSpec extends Specification {

    NamespaceManager namespaceManager
    RestTemplateReLoginDecorated restTemplateFactoryReLoginDecorated
    ECSMgmtNamespacePayload namespace
    ECSConfig ecsConfig

    def setup() {
        namespace = new ECSMgmtNamespacePayload()
        namespace.namespace = "idnamespace"
        restTemplateFactoryReLoginDecorated = Mock()
        ecsConfig = Stub()
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        namespaceManager = new NamespaceManager(ecsConfig)
        namespaceManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecorated
    }

    def "create namespace call proper endpoint"() {
        when:


        namespaceManager.create(namespace)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/namespaces/namespace", HttpMethod.POST, _, _)
    }

    def "list namespace call proper endpoint"() {
        when:
        namespaceManager.list()
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/namespaces", HttpMethod.GET, _, _)
    }

    def "delete namespace call proper endpoint"() {
        when:
        namespaceManager.delete(namespace)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/namespaces/namespace/idnamespace/deactivate", HttpMethod.POST, _, _)
    }

    def "is exists return false when namespace not exists"() {
        when:
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        responseEntity.getStatusCode() >> HttpStatus.BAD_REQUEST
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/namespaces/namespace/idnamespace", HttpMethod.GET, _, _) >> responseEntity
        namespaceManager = new NamespaceManager(ecsConfig)
        namespaceManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecoratedStubbed
        then:
        false == namespaceManager.isExists(namespace)
    }

    def "is exists return true when namespace exists"() {
        when:
        RestTemplateReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/namespaces/namespace/idnamespace", HttpMethod.GET, _, _) >> responseEntity
        namespaceManager = new NamespaceManager(ecsConfig)
        namespaceManager.restTemplateReLoginDecorated = restTemplateFactoryReLoginDecoratedStubbed
        then:
        true == namespaceManager.isExists(namespace)
    }


}
