package com.swisscom.cloud.sb.broker.servicedefinition

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
@EnableConfigurationProperties
@Slf4j
class ServiceDefinitionInitializer {

    private CFServiceRepository cfServiceRepository

    private ServiceDefinitionConfig serviceDefinitionConfig

    private ServiceDefinitionProcessor serviceDefinitionProcessor

    @Autowired
    ServiceDefinitionInitializer(CFServiceRepository cfServiceRepository, ServiceDefinitionConfig serviceDefinitionConfig, ServiceDefinitionProcessor serviceDefinitionProcessor) {
        this.cfServiceRepository = cfServiceRepository
        this.serviceDefinitionConfig = serviceDefinitionConfig
        this.serviceDefinitionProcessor = serviceDefinitionProcessor
    }

    @PostConstruct
    void init() throws Exception {
        List<CFService> cfServiceList = cfServiceRepository.findAll()

        checkForMissingServiceDefinitions(cfServiceList)
        addServiceDefinitions()
    }

    void checkForMissingServiceDefinitions(List<CFService> cfServiceList) {
        def configGuidList = serviceDefinitionConfig.serviceDefinitions.collect {it.guid}

        def guidList = cfServiceList.collect {it.guid}

        if (configGuidList.size() != 0) {
            if (!configGuidList.containsAll(guidList)) {
                throw new RuntimeException("Missing service definition configuration exception. Service list - ${guidList}")
            }
        }
    }

    void addServiceDefinitions() {
        serviceDefinitionConfig.serviceDefinitions.each {
            serviceDefinitionProcessor.createOrUpdateServiceDefinitionFromYaml(it)
        }
    }
}
