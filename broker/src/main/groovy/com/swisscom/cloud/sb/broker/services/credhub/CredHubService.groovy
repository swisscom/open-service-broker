package com.swisscom.cloud.sb.broker.services.credhub

import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.user.UserCredential

interface CredHubService {
    CredentialDetails<UserCredential> writeCredential(String credentialName, String username, String password)

    CredentialDetails<UserCredential> getCredential(String id)

    void deleteCredential(String credentialName)
}
