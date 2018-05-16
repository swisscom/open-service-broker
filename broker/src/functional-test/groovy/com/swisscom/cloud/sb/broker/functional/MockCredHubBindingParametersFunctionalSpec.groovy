package com.swisscom.cloud.sb.broker.functional

import com.swisscom.cloud.sb.IntegrationTestMockingConfig
import com.swisscom.cloud.sb.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import com.swisscom.cloud.sb.broker.util.StringGenerator
import com.swisscom.cloud.sb.broker.util.test.DummySynchronousServiceProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.credhub.core.CredHubException
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.CredentialType
import org.springframework.credhub.support.SimpleCredentialName
import org.springframework.credhub.support.user.UserCredential
import org.springframework.http.HttpStatus

@Import([IntegrationTestMockingConfig])
class MockCredHubBindingParametersFunctionalSpec extends BaseFunctionalSpec {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository
    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService

    private CredHubService credHubService

    def setup() {
        credHubService = serviceBindingPersistenceService.getCredHubService()
        serviceLifeCycler.createServiceIfDoesNotExist('SyncDummyInstancesRetrievable', ServiceProviderLookup.findInternalName(DummySynchronousServiceProvider.class), null, null, null, 0, true, true)
    }

    def cleanupSpec() {
        serviceLifeCycler.cleanup()
    }

    def "provision async service instance and bind with parameters store to mock credhub"() {
        given:
        def serviceBindingGuid = StringGenerator.randomUuid()
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        String username = StringGenerator.randomUuid()
        String password = StringGenerator.randomUuid()

        mockCredHubWriteCredential(serviceBindingGuid, username, password)

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

    def "get binding credentials from CredHub"() {
        given:
        mockCredHubGetCredential()

        when:
        def bindingResponse = serviceBrokerClient.getServiceInstanceBinding(serviceLifeCycler.serviceInstanceId, serviceLifeCycler.serviceBindingId)

        then:
        noExceptionThrown()
        bindingResponse != null
        bindingResponse.body.credentials != null

        def credentials = JsonHelper.parse(bindingResponse.body.credentials, Map) as Map
        credentials.username != null
        credentials.password != null
    }

    def "deprovision async service instance and delete credential from CredHub"() {
        given:
        mockCredHubDeleteCredential()

        when:
        serviceLifeCycler.deleteServiceBindingAndServiceInstaceAndAssert()

        then:
        noExceptionThrown()

        1 * credHubService.deleteCredential(serviceLifeCycler.serviceBindingId)
    }

    def "provision async service instance and bind with parameters and simulate credhub error, credentials store fallback to default"() {
        given:
        def serviceInstanceGuid = StringGenerator.randomUuid()
        def serviceBindingGuid = StringGenerator.randomUuid()
        serviceLifeCycler.setServiceInstanceId(serviceInstanceGuid)
        serviceLifeCycler.setServiceBindingId(serviceBindingGuid)
        serviceLifeCycler.createServiceInstanceAndAssert(0, false, false)

        String username = StringGenerator.randomUuid()
        String password = StringGenerator.randomUuid()

        credHubService.writeCredential(_, _, _) >> {
            throw new CredHubException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        when:
        serviceLifeCycler.bindServiceInstanceAndAssert(null, [username: username, password: password])

        then:
        noExceptionThrown()

        def serviceBinding = serviceBindingRepository.findByGuid(serviceBindingGuid)
        serviceBinding != null
        serviceBinding.credhubCredentialId == null
        serviceBinding.credentials != null

        def credentials = JsonHelper.parse(serviceBinding.credentials, Map) as Map
        credentials.username != null
        credentials.password != null
    }

    private void mockCredHubWriteCredential(String serviceBindingGuid, String username, String password) {
        credHubService.writeCredential(_, _, _) >> new CredentialDetails(StringGenerator.randomUuid(), new SimpleCredentialName(serviceBindingGuid), CredentialType.USER, new UserCredential(username, password))
    }

    private void mockCredHubGetCredential() {
        UserCredential cred = new UserCredential(StringGenerator.randomUuid(), StringGenerator.randomUuid())
        credHubService.getCredential(_) >> new CredentialDetails(StringGenerator.randomUuid(), new SimpleCredentialName(StringGenerator.randomUuid()), CredentialType.USER, cred)
    }

    private void mockCredHubDeleteCredential() {
        credHubService.deleteCredential(_) >> void
    }

}