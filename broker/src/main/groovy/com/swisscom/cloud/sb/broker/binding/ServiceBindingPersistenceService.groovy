package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ApplicationUserRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.credhub.CredHubService
import com.swisscom.cloud.sb.broker.util.JsonHelper
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.Context
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
class ServiceBindingPersistenceService {
    @Autowired
    private ServiceBindingRepository serviceBindingRepository

    @Autowired
    private ServiceDetailRepository serviceDetailRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceContextPersistenceService contextPersistenceService

    @Autowired
    protected ApplicationUserRepository applicationUserRepository

    @Autowired
    private ApplicationContext applicationContext

    ServiceBinding create(ServiceInstance serviceInstance, String credentials, String parameters, String guid, Collection<ServiceDetail> details, Context context, String applicationUser) {
        ServiceBinding serviceBinding = new ServiceBinding()
        serviceBinding.guid = guid
        serviceBinding.parameters = parameters
        serviceBinding.applicationUser = applicationUserRepository.findByUsername(applicationUser)
        handleBindingCredentials(serviceBinding, credentials)
        serviceBindingRepository.save(serviceBinding)
        details?.each {
            ServiceDetail detail ->
                serviceDetailRepository.save(detail)
                serviceBinding.details.add(detail)
        }
        if (context) {
            serviceBinding.serviceContext = contextPersistenceService.findOrCreate(context)
        }
        serviceBindingRepository.save(serviceBinding)
        serviceInstance.bindings.add(serviceBinding)
        serviceInstanceRepository.save(serviceInstance)
        return serviceBinding
    }

    ServiceBinding update(ServiceBinding serviceBinding) {
        serviceBindingRepository.save(serviceBinding)
        serviceBinding.details?.each {
            ServiceDetail detail ->
                serviceDetailRepository.save(detail)
        }
        serviceBindingRepository.save(serviceBinding)
        return serviceBinding
    }


    void delete(ServiceBinding serviceBinding, ServiceInstance serviceInstance) {
        ServiceInstance serviceInstance_new = serviceInstanceRepository.merge(serviceInstance)
        serviceInstance_new.bindings.remove(serviceBinding)
        serviceInstanceRepository.save(serviceInstance_new)
        ServiceBinding serviceBinding_new = serviceBindingRepository.merge(serviceBinding)
        Set<ServiceBinding> details = new HashSet<>(serviceBinding_new.details ?: [])
        details.each {
            ServiceDetail detail ->
                serviceBinding_new.details.remove(detail)
                serviceDetailRepository.delete(detail)
        }
        serviceBindingRepository.delete(serviceBinding_new)

        // delete credential from CredHub
        def credHubService = getCredHubService()
        if (serviceBinding.credhubCredentialId && credHubService) {
            try {
                credHubService.deleteCredential(serviceBinding.guid)
            } catch (Exception e) {
                log.error('Unable to delete CredHub credentials for name: ' + serviceBinding.guid, e)
            }
        }
    }

    def handleBindingCredentials(ServiceBinding serviceBinding, String credentialJson) {
        def credhubService = getCredHubService()
        Map credentials = JsonHelper.parse(credentialJson, Map)
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

    CredHubService getCredHubService() {
        try {
            return applicationContext.getBean(CredHubService)
        } catch (NoSuchBeanDefinitionException e) {
            return null
        }
    }

}
