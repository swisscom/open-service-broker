package com.swisscom.cf.broker.services.ecs.facade

import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.facade.client.ECSManagementClient
import com.swisscom.cf.broker.services.ecs.facade.filters.ECSManagementInputFilter
import com.swisscom.cf.broker.services.ecs.facade.validators.ECSManagementInputValidator
import spock.lang.Specification

class ECSManagementFacadeSpec extends Specification {

    ECSManagementFacade ecsManagementFacade
    ECSManagementInputValidator ecsManagementInputValidator
    ECSManagementInputFilter ecsManagementInputFilter
    ECSManagementClient ecsManagementClient
    Namespace namespace

    def setup() {
        ecsManagementInputValidator = Mock()
        ecsManagementInputFilter = Mock()
        ecsManagementClient = Mock()
        namespace = Mock()
        ecsManagementFacade = new ECSManagementFacade(ecsManagementInputFilter: ecsManagementInputFilter, ecsManagementInputValidator: ecsManagementInputValidator, ecsManagementClient: ecsManagementClient)
    }

    def "facade uses filter"() {
        when:
        ecsManagementFacade.createNamespace(namespace)
        then:
        1 * ecsManagementInputFilter.filter(namespace)
    }


    def "facade uses validation"() {
        when:
        ecsManagementFacade.createNamespace(namespace)
        then:
        1 * ecsManagementInputValidator.validate(namespace)
    }

    def "facade create namespace"() {
        when:
        ecsManagementFacade.createNamespace(namespace)
        then:
        1 * ecsManagementClient.create(namespace)
    }


}
