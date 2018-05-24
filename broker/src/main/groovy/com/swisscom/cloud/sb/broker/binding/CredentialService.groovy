package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class CredentialService {

    @Autowired
    private CredHubCredentialStoreStrategy credHubCredentialStoreStrategy
    @Autowired
    private DefaultCredentialStoreStrategy defaultCredentialStoreStrategy

    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        try {
            if (credHubCredentialStoreStrategy.isCredHubServiceAvailable()) {
                credHubCredentialStoreStrategy.writeCredential(serviceBinding, credentialJson)
            } else {
                defaultCredentialStoreStrategy.writeCredential(serviceBinding, credentialJson)
            }
        } catch (Throwable t) {
            log.error('Unable to store CredHub credential.', t)
            defaultCredentialStoreStrategy.writeCredential(serviceBinding, credentialJson)
        }
    }

    def deleteCredential(ServiceBinding serviceBinding) {
        try {
            if (serviceBinding.credhubCredentialId && credHubCredentialStoreStrategy.isCredHubServiceAvailable()) {
                credHubCredentialStoreStrategy.deleteCredential(serviceBinding)
            } else {
                defaultCredentialStoreStrategy.deleteCredential(serviceBinding)
            }
        } catch (Throwable t) {
            log.error('Unable to delete CredHub credential.', t)
            defaultCredentialStoreStrategy.deleteCredential(serviceBinding)
        }
    }

    String getCredential(ServiceBinding serviceBinding) {
        try {
            if (serviceBinding.credhubCredentialId && credHubCredentialStoreStrategy.isCredHubServiceAvailable()) {
                return credHubCredentialStoreStrategy.getCredential(serviceBinding)
            } else {
                defaultCredentialStoreStrategy.getCredential(serviceBinding)
            }
        } catch (Throwable t) {
            log.error('Unable to get CredHub credential.', t)
            defaultCredentialStoreStrategy.getCredential(serviceBinding)
        }
    }

}
