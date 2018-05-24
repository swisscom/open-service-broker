package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class CredHubCredentialStoreStrategy implements CredentialStoreStrategy {

    @Autowired
    private ApplicationContext applicationContext

    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        def credhubService = getCredHubService()
        Map credentials = JsonHelper.parse(credentialJson, Map) as Map
        def credhubUserCredential = credhubService.writeCredential(serviceBinding.guid, credentials.username as String, credentials.password as String)
        serviceBinding.credhubCredentialId = credhubUserCredential.id
        credentials.username = null
        credentials.password = null
        serviceBinding.credentials = JsonHelper.toJsonString(credentials)
    }

    def deleteCredential(ServiceBinding serviceBinding) {
        def credHubService = getCredHubService()
        credHubService.deleteCredential(serviceBinding.guid)
    }

    String getCredential(ServiceBinding serviceBinding) {
        def credentials = JsonHelper.parse(serviceBinding.credentials, Map) as Map
        def credHubService = getCredHubService()

        def credentialDetails = credHubService.getCredential(serviceBinding.credhubCredentialId)
        credentials.username = credentialDetails.value.username
        credentials.password = credentialDetails.value.password

        return JsonHelper.toJsonString(credentials)
    }

    CredHubService getCredHubService() {
        try {
            return applicationContext.getBean(CredHubService)
        } catch (NoSuchBeanDefinitionException e) {
            return null
        }
    }

    boolean isCredHubServiceAvailable() {
        return getCredHubService() != null
    }

}
