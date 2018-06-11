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
        def credhubJsonCredential = credhubService.writeCredential(serviceBinding.guid, credentials)
        serviceBinding.credhubCredentialId = credhubJsonCredential.id
        serviceBinding.credentials = null
    }

    def deleteCredential(ServiceBinding serviceBinding) {
        def credHubService = getCredHubService()
        credHubService.deleteCredential(serviceBinding.guid)
    }

    String getCredential(ServiceBinding serviceBinding) {
        JsonHelper.toJsonString(getCredHubService().getCredential(serviceBinding.credhubCredentialId).value)
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
