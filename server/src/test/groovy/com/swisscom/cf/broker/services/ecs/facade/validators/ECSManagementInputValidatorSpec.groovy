package com.swisscom.cf.broker.services.ecs.facade.validators

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import com.swisscom.cf.broker.services.ecs.domain.User
import com.swisscom.cf.broker.services.ecs.facade.client.details.NamespaceManager
import com.swisscom.cf.broker.services.ecs.facade.client.details.UserManager
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.NamespaceExistsException
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.NamespaceWrongPrefixException
import com.swisscom.cf.broker.services.ecs.facade.validators.exception.UserExistsException
import spock.lang.Specification

class ECSManagementInputValidatorSpec extends Specification {

    ECSManagementInputValidator ecsManagementInputValidator
    UserManager userManager
    NamespaceManager namespaceManager
    Namespace namespace
    User user
    ECSConfig ecsConfig

    def setup() {
        userManager = Stub()
        namespaceManager = Stub()
        ecsConfig = Stub()
        ecsManagementInputValidator = new ECSManagementInputValidator(ecsConfig: ecsConfig, userManager: userManager, namespaceManager: namespaceManager)
        namespace = new Namespace()
        user = new User()
    }

    def "throws exception for namespace exists"() {
        when:
        namespaceManager.isExists(_) >> true
        ecsManagementInputValidator.validate(namespace)
        then:
        thrown NamespaceExistsException
    }


    def "throws exception for namespace not prefixed"() {
        when:
        namespace.namespace = ""
        namespaceManager.isExists(_) >> false
        ecsConfig.getEcsManagementNamespacePrefix() >> "PREFIX"
        ecsManagementInputValidator.validate(namespace)
        then:
        thrown NamespaceWrongPrefixException
    }

    def "ok for namespace prefixed"() {
        when:
        namespace.namespace = "PREFIX"
        namespaceManager.isExists(_) >> false
        ecsConfig.getEcsManagementNamespacePrefix() >> "PREFIX"
        ecsManagementInputValidator.validate(namespace)
        then:
        true
    }


    def "throws exception for user exists"() {
        when:
        userManager.isExists(_) >> true
        ecsManagementInputValidator.validate(user)
        then:
        thrown UserExistsException
    }


}
