package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyServiceProviderExtensions

class ExtensionProviderFunctionalSpec extends BaseFunctionalSpec{

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('extensionServiceProvider', ServiceProviderLookup.findInternalName(DummyServiceProviderExtensions.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "create service and stuff"(){
        when:
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)
        then:
        noExceptionThrown()
    }
}
