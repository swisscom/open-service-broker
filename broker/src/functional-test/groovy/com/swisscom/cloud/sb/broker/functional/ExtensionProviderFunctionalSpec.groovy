package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyExtension.DummyExtensionsServiceProvider

import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import org.springframework.http.ResponseEntity

class ExtensionProviderFunctionalSpec extends BaseFunctionalSpec{

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('extensionServiceProvider', ServiceProviderLookup.findInternalName(DummyExtensionsServiceProvider.class), null, null,"dummyExtensions")
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "Create service and verify extension"(){
        when:
        ResponseEntity<ProvisionResponseDto> res = serviceLifeCycler.provision(false, null, null)
        then:
        "DummyExtensionURL" == res.body.extension_apis[0].discovery_url
    }

//    def "Execute async extension"(){
//
//    }

    def "Execute sync extension"(){
        when:
        def res = serviceBrokerClient.lockUser(serviceLifeCycler.serviceInstanceId)
        println("res = " + res)
        then:
        noExceptionThrown()
    }
}
