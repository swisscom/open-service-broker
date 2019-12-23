package com.swisscom.cloud.sb.broker.services.credential

import com.swisscom.cloud.sb.broker.BaseSpecification
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.credhub.core.CredHubException
import org.springframework.http.HttpStatus
import spock.lang.Unroll

class CredHubCredentialStoreTest extends BaseSpecification {
    @Autowired
    private CredentialStore credentialStore

    @Unroll
    def "should save credential #credential to credhub with key #bindingId"() {
        given:
        ServiceBinding serviceBinding = new ServiceBinding(guid: bindingId)

        when:
        ServiceBinding response = credentialStore.save(serviceBinding, credential)

        then:
        credentialStore.get(serviceBinding) == credential
        response.getCredhubCredentialId().size() > 0

        where:
        bindingId                    | credential
        'test'                       | '{"test":"hello"}'
        UUID.randomUUID().toString() | '{"test":"hello"}'
    }

    @Unroll
    def "should fail to save credential #credential to credhub with key #bindingId"() {
        given:
        ServiceBinding serviceBinding = new ServiceBinding(guid: bindingId)

        when:
        ServiceBinding response = credentialStore.save(serviceBinding, credential)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.getMessage() == errorMessage
        response == null
        serviceBinding.getCredhubCredentialId() == null

        where:
        bindingId                    | credential         | errorMessage
        null                         | '{"test":"hello"}' | "ServiceBinding key must not be empty"
        ""                           | '{"test":"hello"}' | "ServiceBinding key must not be empty"
        UUID.randomUUID().toString() | null               | "Credential must not be empty"
        UUID.randomUUID().toString() | ''                 | "Credential must not be empty"
        UUID.randomUUID().toString() | '{}'               | "credentials may not be null"

    }

    @Unroll
    def "should delete credential with key #bindingId"() {
        given:
        ServiceBinding serviceBinding = new ServiceBinding(guid: bindingId)
        credentialStore.save(serviceBinding, credential)
        String credhubCredentialId = serviceBinding.getCredhubCredentialId()

        when: "deleting the credential"
        credentialStore.delete(serviceBinding)

        and: "trying to get the credential to confirm it cannot be found"
        serviceBinding.setCredhubCredentialId(credhubCredentialId)
        credentialStore.get(serviceBinding)

        then:
        def ex = thrown(CredHubException)
        ex.getStatusCode() == HttpStatus.NOT_FOUND

        where:
        bindingId                    | credential
        UUID.randomUUID().toString() | '{"test":"hello"}'
    }
}
