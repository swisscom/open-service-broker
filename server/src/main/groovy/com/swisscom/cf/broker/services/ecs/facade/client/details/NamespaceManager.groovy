package com.swisscom.cf.broker.services.ecs.facade.client.details

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespaceResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.NamespaceToECSMgmtNamespace
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import org.springframework.http.HttpMethod


class NamespaceManager {

    final RestTemplateFactoryReLoginDecorated<ECSMgmtNamespacePayload, ECSMgmtNamespaceResponse> restTemplateFactoryReLoginDecorated
    private final NamespaceToECSMgmtNamespace namespaceToECSMgmtNamespace

    def create(Namespace namespace) {
        restTemplateFactoryReLoginDecorated.exchange("URL", HttpMethod.POST, namespaceToECSMgmtNamespace.adapt(namespace), ECSMgmtNamespaceResponse.class)
    }
}
