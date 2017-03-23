package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputDecorator

import spock.lang.Specification

class ECSManagementFacadeSpec extends Specification {

    ECSManagementFacade ecsManagementFacade
    ECSManagementInputDecorator ecsManagementInputFilter
    ECSManagementClient ecsManagementClient
    Namespace namespace

    def setup() {
        ecsManagementInputFilter = Mock()
        ecsManagementClient = Mock()
        namespace = Mock()
        ecsManagementFacade = new ECSManagementFacade(ecsManagementInputFilter: ecsManagementInputFilter, ecsManagementClient: ecsManagementClient)
    }

    def "facade uses filter"() {
        when:
        ecsManagementFacade.createNamespace(namespace)
        then:
        1 * ecsManagementInputFilter.decorate(namespace)
    }

    def "facade create namespace"() {
        when:
        ecsManagementFacade.createNamespace(namespace)
        then:
        1 * ecsManagementClient.create(namespace)
    }


}
