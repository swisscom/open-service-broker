package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyExtension.DummyExtensionsServiceProvider

import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import org.springframework.http.ResponseEntity

class ExtensionProviderFunctionalSpec extends BaseFunctionalSpec{

    DummyExtensionsServiceProvider dummyExtensionsServiceProvider

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('extensionServiceProvider', ServiceProviderLookup.findInternalName(DummyExtensionsServiceProvider.class), null, null,"dummyExtensions")
//        serviceLifeCycler.createServiceIfDoesNotExist('dummyExtensionsServiceProvider', ServiceProviderLookup.findInternalName(DummyExtensionsServiceProvider.class))
//        dummyExtensionsServiceProvider = new DummyExtensionsServiceProvider()
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Create service and verify extension"(){
        when:
        ResponseEntity<ProvisionResponseDto> res = serviceLifeCycler.provision(false, null, null)
        println("res = " + res.body.extension_apis[0].discovery_url)
//        dummyExtensionsServiceProvider.lockUser()
        then:
        "DummyExtensionURL" == res.body.extension_apis[0].discovery_url
    }
}
