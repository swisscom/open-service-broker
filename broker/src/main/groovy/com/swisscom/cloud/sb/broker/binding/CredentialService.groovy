package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CredentialService {

    @Autowired
    private CredentialStoreFactory credentialStoreFactory

    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        credentialStoreFactory.object.writeCredential(serviceBinding, credentialJson)
    }

    def deleteCredential(ServiceBinding serviceBinding) {
        credentialStoreFactory.object.deleteCredential(serviceBinding)
    }

    String getCredential(ServiceBinding serviceBinding) {
        credentialStoreFactory.object.getCredential(serviceBinding)
    }

}
