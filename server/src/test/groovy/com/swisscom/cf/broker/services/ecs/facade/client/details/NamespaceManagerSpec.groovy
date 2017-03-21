package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespaceResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.NamespaceToECSMgmtNamespace
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class NamespaceManagerSpec extends Specification {

    NamespaceManager namespaceManager
    RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecorated
    NamespaceToECSMgmtNamespace namespaceToECSMgmtNamespace
    Namespace namespace
    ECSConfig ecsConfig

    def setup() {
        namespace = new Namespace()
        restTemplateFactoryReLoginDecorated = Mock()
        namespaceToECSMgmtNamespace = Stub()
        ecsConfig = Stub()
        namespaceManager = new NamespaceManager(namespaceToECSMgmtNamespace: namespaceToECSMgmtNamespace, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecorated)
    }

    def "create namespace call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        namespace.namespace = "idnamespace"
        namespaceManager.create(namespace)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/namespaces/namespace", HttpMethod.POST, _, ECSMgmtNamespaceResponse.class)
    }

    def "list namespace call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        namespaceManager.list()
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/namespaces", HttpMethod.GET, null, ECSMgmtNamespaceResponse.class)
    }

    def "delete namespace call proper endpoint"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        namespace.namespace = "idnamespace"
        namespaceManager.delete(namespace)
        then:
        1 * restTemplateFactoryReLoginDecorated.exchange("http.server.com/object/namespaces/namespace/idnamespace/deactivate", HttpMethod.POST, null, ECSMgmtNamespaceResponse.class)
    }

    def "is exists return false when namespace not exists"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        namespace.namespace = "idnamespace"
        RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        responseEntity.getStatusCode() >> HttpStatus.BAD_REQUEST
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/namespaces/namespace/idnamespace", HttpMethod.GET, null, ECSMgmtNamespaceResponse.class) >> responseEntity
        namespaceManager = new NamespaceManager(namespaceToECSMgmtNamespace: namespaceToECSMgmtNamespace, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecoratedStubbed)
        then:
        false == namespaceManager.isExists(namespace)
    }

    def "is exists return true when namespace exists"() {
        when:
        ecsConfig.getEcsManagementBaseUrl() >> "http.server.com"
        namespace.namespace = "idnamespace"
        RestTemplateFactoryReLoginDecorated restTemplateFactoryReLoginDecoratedStubbed = Stub()
        ResponseEntity responseEntity = Stub()
        restTemplateFactoryReLoginDecoratedStubbed.exchange("http.server.com/object/namespaces/namespace/idnamespace", HttpMethod.GET, null, ECSMgmtNamespaceResponse.class) >> responseEntity
        namespaceManager = new NamespaceManager(namespaceToECSMgmtNamespace: namespaceToECSMgmtNamespace, ecsConfig: ecsConfig, restTemplateFactoryReLoginDecorated: restTemplateFactoryReLoginDecoratedStubbed)
        then:
        true == namespaceManager.isExists(namespace)
    }


}
