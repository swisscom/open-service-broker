package com.swisscom.cf.broker.services.ecs.facade.client.commands

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespace
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespaceResponse
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.adapters.NamespaceToECSMgmtNamespace
import com.swisscom.cf.broker.services.ecs.facade.client.rest.RestTemplateFactoryReLoginDecorated
import com.swisscom.cf.broker.util.RestTemplateFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod


class CreateNamespace {

    final RestTemplateFactoryReLoginDecorated<ECSMgmtNamespace, ECSMgmtNamespaceResponse> restTemplateFactoryReLoginDecorated
    private final NamespaceToECSMgmtNamespace namespaceToECSMgmtNamespace

    def create(Namespace namespace) {
        restTemplateFactoryReLoginDecorated.exchange("URL", HttpMethod.POST, namespaceToECSMgmtNamespace.adapt(namespace), ECSMgmtNamespaceResponse.class)
    }
}
