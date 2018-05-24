package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@Component
@CompileStatic
class DefaultCredentialStoreStrategy implements CredentialStoreStrategy {

    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        serviceBinding.credentials = credentialJson
    }

    def deleteCredential(ServiceBinding serviceBinding) {
    }

    String getCredential(ServiceBinding serviceBinding) {
        return serviceBinding.credentials
    }

}
