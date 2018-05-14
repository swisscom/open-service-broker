package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.binding.ServiceInstanceBindingResponseDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Slf4j
@Component
class ServiceInstanceBindingDtoConverter extends AbstractGenericConverter<ServiceBinding, ServiceInstanceBindingResponseDto> {

    @Autowired
    private ApplicationContext applicationContext

    @Override
    void convert(ServiceBinding source, ServiceInstanceBindingResponseDto prototype) {
        Object credentials = new JsonSlurper().parseText(source.credentials)
        def credHubService = getCredHubService()

        if (source.credhubCredentialId && credHubService) {
            def credentialDetails = credHubService.getCredential(source.credhubCredentialId)
            credentials.username = credentialDetails.value.username
            credentials.password = credentialDetails.value.password
        }

        prototype.credentials = new JsonBuilder(credentials).toString()
        prototype.parameters = source.parameters
        prototype.details = source.details
        prototype.routeServiceUrl = null
        prototype.syslogDrainUrl = null
        prototype.volumeMounts = null
    }

    private CredHubService getCredHubService() {
        try {
            return applicationContext.getBean(CredHubService)
        } catch (NoSuchBeanDefinitionException e) {
            return null
        }
    }

}