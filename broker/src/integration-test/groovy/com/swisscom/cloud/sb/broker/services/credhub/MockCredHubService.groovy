package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.util.StringGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.CredentialType
import org.springframework.credhub.support.SimpleCredentialName
import org.springframework.credhub.support.user.UserCredential

@CompileStatic
@Slf4j
class MockCredHubService implements CredHubService {

    @Override
    CredentialDetails<UserCredential> writeCredential(String credentialName, String username, String password) {
        UserCredential cred = new UserCredential(username, password)
        return new CredentialDetails(StringGenerator.randomUuid(), new SimpleCredentialName(credentialName), CredentialType.USER, cred)
    }

    @Override
    CredentialDetails<UserCredential> getCredential(String id) {
        UserCredential cred = new UserCredential(StringGenerator.randomUuid(), StringGenerator.randomUuid())
        return new CredentialDetails(id, new SimpleCredentialName(StringGenerator.randomUuid()), CredentialType.USER, cred)
    }

    @Override
    void deleteCredential(String credentialName) {
        log.info("Deleted mock credentials: " + credentialName)
    }

}
