package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.services.common.ServiceProviderLookup
import com.swisscom.cf.broker.util.test.DummySynchronousServiceProvider

class BindingParametersFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance"() {
        given:
        serviceLifeCycler.createServiceInstanceAndAssert(false, false)

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