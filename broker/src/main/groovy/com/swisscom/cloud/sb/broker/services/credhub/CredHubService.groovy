package com.swisscom.cloud.sb.broker.services.credhub

import org.springframework.credhub.support.CredentialDetails
import org.springframework.credhub.support.json.JsonCredential

interface CredHubService {
    CredentialDetails<JsonCredential> writeCredential(String credentialName, Map<String, String> credentials)

    CredentialDetails<JsonCredential> getCredential(String id)

    void deleteCredential(String credentialName)
}
