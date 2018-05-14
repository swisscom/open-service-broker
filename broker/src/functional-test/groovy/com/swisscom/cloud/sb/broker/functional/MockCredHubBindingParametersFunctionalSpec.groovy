package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.credhub.MockCredHubAutoConfiguration
import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.beans.factory.annotation.Autowired

class MockCredHubBindingParametersFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    def setupSpec() {
        System.setProperty(MockCredHubAutoConfiguration.SYSTEM_PROPERTY_MOCK_CREDHUB, MockCredHubAutoConfiguration.SYSTEM_PROPERTY_MOCK_CREDHUB)
    }

    def setup() {
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummy', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class))
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind with parameters store to mock credhub"() {
        given:
        def serviceBindingGuid = UUID.randomUUID().toString()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        String username = StringGenerator.randomUuid()
        String password = StringGenerator.randomUuid()

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, [username: username, password: password])

        then:
        noExceptionThrown()

        def serviceBinding = serviceBindingRepository.findByGuid(serviceBindingGuid)
        serviceBinding != null
        serviceBinding.credhubCredentialId != null
        serviceBinding.credentials != null

        def credentials = JsonHelper.parse(serviceBinding.credentials, Map) as Map
        credentials.username == null
        credentials.password == null
    }

}