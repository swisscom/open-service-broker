package com.swisscom.cf.broker.services.ecs.facade.filters

import com.swisscom.cf.broker.services.ecs.config.ECSConfig
import com.swisscom.cf.broker.services.ecs.domain.Namespace
import spock.lang.Specification

class ECSManagementInputDecoratorSpec extends Specification {

    ECSConfig ecsConfig
    ECSManagementInputDecorator ecsManagementInputFilter
    Namespace namespace

    def setup() {
        ecsConfig = Stub()
        ecsConfig.getEcsManagementNamespacePrefix() >> "PREFIX"
        ecsConfig.getEcsDefaultDataServicesVpool() >> "POOL"
        ecsManagementInputFilter = new ECSManagementInputDecorator(ecsConfig: ecsConfig)
        namespace = new Namespace()
    }

    def "filter for namespace set PREFIX"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getNamespace() == "PREFIX"
    }

    def "filter for namespace set default_data_services_vpool"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getDefaultDataServicesVpool() == "POOL"
    }

    def "filter for namespace set encryption"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getIsEncryptionEnabled() == false
    }

    def "filter for namespace set default_bucket_block_size"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getDefaultBucketBlockSize() == -1
    }

    def "filter for namespace set is_stale_allowed"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getIsStaleAllowed() == true
    }

    def "filter for namespace set compliance_enabled"() {
        when:
        ecsManagementInputFilter.decorate(namespace)
        then:
        namespace.getComplianceEnabled() == false
    }

}
