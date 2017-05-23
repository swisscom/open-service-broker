package com.swisscom.cf.broker.binding

import com.swisscom.cf.broker.model.ServiceBinding
import com.swisscom.cf.broker.model.ServiceDetail
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.model.repository.ServiceBindingRepository
import com.swisscom.cf.broker.model.repository.ServiceDetailRepository
import com.swisscom.cf.broker.model.repository.ServiceInstanceRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
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

    public ServiceBinding create(ServiceInstance serviceInstance, String credentials, String guid, Collection<ServiceDetail> details) {
        ServiceBinding serviceBinding = new ServiceBinding()
        serviceBinding.guid = guid
        serviceBinding.credentials = credentials
        serviceBindingRepository.save(serviceBinding)
        details?.each {
            ServiceDetail detail ->
                serviceDetailRepository.save(detail)
                serviceBinding.details.add(detail)
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
