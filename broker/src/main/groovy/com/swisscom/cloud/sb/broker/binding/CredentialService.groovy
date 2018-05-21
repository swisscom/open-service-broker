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
class CredentialService {

    @Autowired
    private ApplicationContext applicationContext

    /**
     * Write Credential to CredHub if CredHub configuration is specified, otherwise fallback to ServiceBinding.credentials field.
     * @param serviceBinding
     * @param credentialJson
     */
    def writeCredential(ServiceBinding serviceBinding, String credentialJson) {
        def credhubService = getCredHubService()
        Map credentials = JsonHelper.parse(credentialJson, Map) as Map
        if (credhubService && credentials?.username && credentials?.password) {
            try {
                def credhubUserCredential = credhubService.writeCredential(serviceBinding.guid, credentials.username as String, credentials.password as String)
                serviceBinding.credhubCredentialId = credhubUserCredential.id
                credentials.username = null
                credentials.password = null
                serviceBinding.credentials = JsonHelper.toJsonString(credentials)
            } catch (Exception e) {
                log.error('Unable to store CredHub credential', e)
                serviceBinding.credentials = credentialJson
            }
        } else {
            serviceBinding.credentials = credentialJson
        }
    }

    /**
     * Delete credential from CredHub if ServiceBinding.credhubCredentialId is set and CredHub configuration is specified.
     * @param serviceBinding
     */
    def deleteCredential(ServiceBinding serviceBinding) {
        def credHubService = getCredHubService()
        if (serviceBinding.credhubCredentialId && credHubService) {
            try {
                credHubService.deleteCredential(serviceBinding.guid)
            } catch (Exception e) {
                log.error('Unable to delete CredHub credentials for name: ' + serviceBinding.guid, e)
            }
        }
    }

    /**
     * Get Credential from CredHub if ServiceBinding.credhubCredentialId is set and CredHub configuration is specified,
     * otherwise get from ServiceBinding.credentials field.
     * @param serviceBinding
     * @return JSON string of credentials
     */
    String getCredential(ServiceBinding serviceBinding) {
        def credentials = JsonHelper.parse(serviceBinding.credentials, Map) as Map
        def credHubService = getCredHubService()

        if (serviceBinding.credhubCredentialId && credHubService) {
            def credentialDetails = credHubService.getCredential(serviceBinding.credhubCredentialId)
            credentials.username = credentialDetails.value.username
            credentials.password = credentialDetails.value.password
        }

        return JsonHelper.toJsonString(credentials)
    }

    CredHubService getCredHubService() {
        try {
            return applicationContext.getBean(CredHubService)
        } catch (NoSuchBeanDefinitionException e) {
            return null
        }
    }

}
