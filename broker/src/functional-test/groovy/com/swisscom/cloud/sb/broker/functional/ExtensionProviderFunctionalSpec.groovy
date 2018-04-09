package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.test.DummyExtension.DummyExtensionsServiceProvider

import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import org.springframework.http.ResponseEntity
import org.yaml.snakeyaml.Yaml

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

    def "Get api docs"(){
        when:
        String res = serviceBrokerClient.getApi(serviceLifeCycler.serviceInstanceId).body
        Yaml parser = new Yaml()
        parser.load(res)
        then:
        noExceptionThrown()
    }

    def "Execute async extension"(){
        when:
        def res = serviceBrokerClient.unlockUser(serviceLifeCycler.serviceInstanceId)
        println("res = " + res)
        then:
        noExceptionThrown()
    }

    def "Execute sync extension"(){
        when:
        def res = serviceBrokerClient.lockUser(serviceLifeCycler.serviceInstanceId)
        println("res = " + res)
        then:
        noExceptionThrown()
    }
}
