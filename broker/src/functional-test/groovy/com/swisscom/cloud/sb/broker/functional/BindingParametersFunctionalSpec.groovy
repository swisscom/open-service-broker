package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.cloud.servicebroker.model.CloudFoundryContext

class BindingParametersFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance with Context"() {
        given:
        def context = new CloudFoundryContext("org_id", "space_id")
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false, null, context)

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, ['key1': 'value1'])

        then:
        noExceptionThrown()
    }

    def "deprovision async service instance"() {
        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()

        then:
        noExceptionThrown()
    }
}