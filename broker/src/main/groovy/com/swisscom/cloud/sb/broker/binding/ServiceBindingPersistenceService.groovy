package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ServiceBindingRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.Context
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

    ServiceBinding create(ServiceInstance serviceInstance, String credentials, String parameters, String guid, Collection<ServiceDetail> details, Context context) {
        ServiceBinding serviceBinding = new ServiceBinding()
        serviceBinding.guid = guid
        serviceBinding.credentials = credentials
        serviceBinding.parameters = parameters
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

    public void delete(ServiceBinding serviceBinding, ServiceInstance serviceInstance) {
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
    }
}
