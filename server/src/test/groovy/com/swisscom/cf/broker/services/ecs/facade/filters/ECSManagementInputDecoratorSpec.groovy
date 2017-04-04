package com.swisscom.cf.broker.services.ecs.facade.filters

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtNamespacePayload
import com.swisscom.cf.broker.services.ecs.facade.client.dtos.ECSMgmtUserPayload
import spock.lang.Specification

class ECSManagementInputDecoratorSpec extends Specification {

    ECSConfig ecsConfig
    ECSManagementInputDecorator ecsManagementInputFilter
    ECSMgmtNamespacePayload namespace

    def setup() {
        ecsConfig = Stub()
        ecsConfig.getEcsManagementNamespacePrefix() >> "123456"
        ecsConfig.getEcsManagementEnvironmentPrefix() >> "789012"
        ecsConfig.getEcsDefaultDataServicesVpool() >> "POOL"
        ecsConfig.getEcsDefaultDataServicesVpool() >> "POOL"
        ecsManagementInputFilter = new ECSManagementInputDecorator(ecsConfig: ecsConfig)
        namespace = new ECSMgmtNamespacePayload()
    }

    def "filter for namespace set prefix"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getNamespace().contains("123456")
    }

    def "filter for namespace set environment prefix"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getNamespace().contains("789012")
    }

    def "filter for namespace set hex name"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getNamespace().matches("[0-9a-fA-F]+")
    }

    def "filter for namespace set proper length name"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getNamespace().length() == 26
    }

    def "filter for namespace set default_data_services_vpool"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getDefault_data_services_vpool() == "POOL"
    }

    def "filter for namespace set encryption"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getIs_encryption_enabled() == false
    }

    def "filter for namespace set default_bucket_block_size"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getDefault_bucket_block_size() == -1
    }

    def "filter for namespace set is_stale_allowed"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getIs_stale_allowed() == true
    }

    def "filter for namespace set compliance_enabled"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getCompliance_enabled() == false
    }

    def "user is sufficed by decorator"() {
        when:
        ECSMgmtUserPayload user = new ECSMgmtUserPayload(user: "namespaceHex")
        ecsManagementInputFilter.decorate(user)
        then:
        user.getUser().matches("namespaceHex-[0-9a-fA-F]{4}")
    }

}
