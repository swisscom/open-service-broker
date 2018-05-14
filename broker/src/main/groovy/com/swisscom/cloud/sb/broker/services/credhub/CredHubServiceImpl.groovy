package com.swisscom.cloud.sb.broker.services.credhub

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.credhub.core.CredHubOperations
import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.SimpleCredentialName
import org.springframework.credhub.support.user.UserCredential
import org.springframework.credhub.support.user.UserCredentialRequest

@CompileStatic
@Slf4j
class CredHubServiceImpl implements CredHubService {

    @Autowired
    private CredHubOperations credHubOperations

    @Override
    CredentialDetails<UserCredential> writeCredential(String credentialName, String username, String password) {
        log.info("Writing new CredHub Credential for name: ${credentialName}, username: ${username}")
        UserCredential userCredential = new UserCredential(username, password)
        UserCredentialRequest request =
                UserCredentialRequest.builder()
                        .overwrite(true)
                        .name(new SimpleCredentialName('/' + credentialName))
                        .value(userCredential)
                        .build()

        credHubOperations.write(request)
    }

    @Override
    CredentialDetails<UserCredential> getCredential(String id) {
        log.info("Get CredHub credentials for id: ${id}")
        credHubOperations.getById(id, UserCredential)
    }

    @Override
    void deleteCredential(String credentialName) {
        log.info("Delete CredHub credentials for name: ${credentialName}")
        credHubOperations.deleteByName(new SimpleCredentialName('/' + credentialName))
    }

}
