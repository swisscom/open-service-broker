package com.swisscom.cloud.sb.broker.services.credhub

import com.swisscom.cloud.sb.broker.binding.ServiceBindingPersistenceService
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Component
@EnableConfigurationProperties
@Slf4j
@Transactional
class CredHubMigrationInitializer {

    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private ServiceBindingPersistenceService serviceBindingPersistenceService

    @PostConstruct
    void init() throws Exception {
        storeCredHubCredential()
    }

    void storeCredHubCredential() {
        def credHubService = serviceBindingPersistenceService.getCredHubService()
        if (!credHubService) {
            return
        }

        def bindings = serviceBindingRepository.findNotMigratedCredHubBindings()
        log.info("Starting ServiceBinding credential migration to CredHub. Found: ${bindings?.size()} bindings to migrate.")
        bindings.each {
            it ->
                def serviceBinding = it
                serviceBindingPersistenceService.handleBindingCredentials(serviceBinding, serviceBinding.credentials)
                serviceBindingRepository.merge(serviceBinding)
        }
        serviceBindingRepository.flush()
    }

}
