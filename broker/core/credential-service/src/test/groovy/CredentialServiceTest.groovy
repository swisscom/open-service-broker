import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.services.credential.CredentialService
import com.swisscom.cloud.sb.broker.services.credential.DefaultCredentialStore
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests CredentialService with DefaultCredentialStore as backend
 */
class CredentialServiceTest extends Specification {

    CredentialService sut

    void setup() {
        sut = new CredentialService(DefaultCredentialStore.create())
    }

    void 'should save credentials to service binding'() {
        when:
        ServiceBinding result = sut.writeCredential(serviceBinding, credential)

        then:
        result != null
        result.getCredentials() == '{"test":"value"}'

        where:
        serviceBinding       | credential
        new ServiceBinding() | '{"test":"value"}'
    }

    @Unroll
    void 'should fail to save credentials with invalid binding: #serviceBinding and credentials: #credential'() {
        when:
        ServiceBinding result = sut.writeCredential(serviceBinding, credential)

        then:
        result == null
        def ex = thrown(IllegalArgumentException)
        ex.getMessage() == errorMessage


        where:
        serviceBinding       | credential         | errorMessage
        null                 | '{"test":"value"}' | "service binding must not be null"
        new ServiceBinding() | 'test'             | "credential must be valid json"
        new ServiceBinding() | null               | "credential must not be null"
    }

    void 'should delete credential from service binding'() {
        given:
        sut.writeCredential(serviceBinding, credential)

        when:
        ServiceBinding result = sut.deleteCredential(serviceBinding)

        then:
        result != null
        result.getCredentials() == null

        where:
        serviceBinding       | credential
        new ServiceBinding() | '{"test":"value"}'
    }

    void 'should fail to delete credential when servicebinding is invalid'() {
        given:
        sut.writeCredential(serviceBinding, credential)

        when:
        ServiceBinding result = sut.deleteCredential(null)

        then:
        result == null
        def ex = thrown(IllegalArgumentException)
        ex.getMessage() == errorMessage

        where:
        serviceBinding       | credential         | errorMessage
        new ServiceBinding() | '{"test":"value"}' | "service binding must not be null"
    }

    void 'should get saved credential'() {
        given:
        sut.writeCredential(serviceBinding, credential)

        when:
        String result = sut.getCredential(serviceBinding)

        then:
        result == credential

        where:
        serviceBinding       | credential
        new ServiceBinding() | '{"test":"value"}'
    }

    void 'should fail to get credential from invalid servicebinding'() {
        given:
        sut.writeCredential(serviceBinding, credential)

        when:
        String result = sut.getCredential(null)

        then:
        result == null
        def ex = thrown(IllegalArgumentException)
        ex.getMessage() == errorMessage

        where:
        serviceBinding       | credential         | errorMessage
        new ServiceBinding() | '{"test":"value"}' | "service binding must not be null"
    }
}
