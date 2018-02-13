package com.swisscom.cloud.sb.broker.provisioning

import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.DeprovisionRequestRepository
import com.swisscom.cloud.sb.broker.model.repository.ProvisionRequestRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
@CompileStatic
class ProvisioningPersistenceService {
    @Autowired
    private ProvisionRequestRepository provisionRequestRepository

    @Autowired
    private DeprovisionRequestRepository deprovisionRequestRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceDetailRepository serviceDetailRepository

    def saveProvisionRequest(ProvisionRequest provisionRequest) {
        provisionRequestRepository.save(provisionRequest)
    }

    def saveDeprovisionRequest(DeprovisionRequest deprovisionRequest) {
        deprovisionRequestRepository.save(deprovisionRequest)
    }

    def ServiceInstance createServiceInstance(ProvisionRequest provisionRequest) {
        ServiceInstance instance = new ServiceInstance()
        instance.guid = provisionRequest.serviceInstanceGuid
        instance.org = provisionRequest.organizationGuid
        instance.space = provisionRequest.spaceGuid
        instance.plan = provisionRequest.plan
        serviceInstanceRepository.save(instance)

        // set parent service instance if specified
        setParentServiceInstance(provisionRequest, instance)

        return instance
    }

    private ServiceInstance setParentServiceInstance(ProvisionRequest provisionRequest, ServiceInstance instance) {
        ServiceInstance parentInstance = null
        def jsonSlurper = new JsonSlurper()
        def parameters = jsonSlurper.parseText(provisionRequest.parameters) as Map
        def parentServiceInstanceGuid = parameters?.parentAlias as String
        if (parentServiceInstanceGuid) {
            parentInstance = serviceInstanceRepository.findByGuid(parentServiceInstanceGuid)
            if (!parentInstance) {
                throw new IllegalArgumentException("Unknown parent ServiceInstance specified: ${parentServiceInstanceGuid}")
            }
            instance.parentServiceInstance = parentInstance
        }
        serviceInstanceRepository.merge(instance)

        // merge parent instance with children
        if (parentInstance) {
            parentInstance.childs << instance
            serviceInstanceRepository.merge(parentInstance)
        }

        parentInstance
    }

    def ServiceInstance createServiceInstance(ProvisionRequest provisionRequest, ProvisionResponse provisionResponse) {
        ServiceInstance instance = createServiceInstance(provisionRequest)
        instance.completed = !provisionResponse.isAsync

        return updateServiceDetails(provisionResponse.details, instance)
    }

    ServiceInstance updateServiceDetails(
            final Collection<ServiceDetail> details, final ServiceInstance instance) {
        details?.each {
            ServiceDetail detail ->
                if (detail.isUniqueKey()) {
                    removeExistingServiceDetailsForKey(detail, instance)
                }
                serviceDetailRepository.save(detail)
                instance.details.add(detail)
        }
        serviceInstanceRepository.save(instance)
    }

    private void removeExistingServiceDetailsForKey(ServiceDetail newServiceDetail, ServiceInstance instance) {
        def existingDetails = instance.details.findAll { it.key == newServiceDetail.key }
        existingDetails?.each {
            ServiceDetail existing ->
                instance.details.remove(existing)
                serviceDetailRepository.delete(existing)
        }
        serviceInstanceRepository.save(instance)
    }

    def ServiceInstance createServiceInstanceOrUpdateDetails(ProvisionRequest provisionRequest, ProvisionResponse provisionResponse) {
        def serviceInstace = getServiceInstance(provisionRequest.serviceInstanceGuid)
        if (serviceInstace) {
            return updateServiceDetails(provisionResponse.details, serviceInstace)
        } else {
            return createServiceInstance(provisionRequest, provisionResponse)
        }
    }

    ServiceInstance updateServiceInstanceCompletion(ServiceInstance instance, boolean completed) {
        instance = serviceInstanceRepository.merge(instance)
        instance.completed = completed
        serviceInstanceRepository.save(instance)
        return instance
    }

    def markServiceInstanceAsDeleted(ServiceInstance instance) {
        instance = serviceInstanceRepository.merge(instance)
        instance.deleted = true
        serviceInstanceRepository.save(instance)
    }

    def ServiceInstance getServiceInstance(String guid) {
        return serviceInstanceRepository.findByGuid(guid)
    }

    def deleteServiceInstance(ServiceInstance serviceInstance) {
        Set<ServiceDetail> details = new HashSet<ServiceDetail>(serviceInstance.details ?: new ArrayList<ServiceDetail>())
        details.each {
            ServiceDetail detail ->
                serviceInstance.details.remove(detail)
                serviceDetailRepository.delete(detail)
        }
        serviceInstanceRepository.delete(serviceInstance)
    }

    def deleteServiceInstanceAndCorrespondingDeprovisionRequestIfExists(ServiceInstance serviceInstance) {
        log.info("Delete request for:${serviceInstance.toString()}")
        removeDeprovisionRequestIfExists(serviceInstance.guid)
        deleteServiceInstance(serviceInstance)
    }

    def removeProvisionRequestIfExists(String guid) {
        ProvisionRequest provisionRequest = provisionRequestRepository.findByServiceInstanceGuid(guid)
        if (provisionRequest) {
            log.info("Deleting provisionRequest with id:${guid}")
            provisionRequestRepository.delete(provisionRequest)
        } else {
            log.info("Found no provisionRequest with id:${guid} to delete")
        }
    }

    def removeDeprovisionRequestIfExists(String guid) {
        DeprovisionRequest deprovisionRequest = deprovisionRequestRepository.findByServiceInstanceGuid(guid)
        if (deprovisionRequest) {
            log.info("Deleting deprovisionRequest(s) with id:${guid}")
            deprovisionRequestRepository.delete(deprovisionRequest)
        } else {
            log.info("Found no deprovisionRequest with id:${guid} to delete")
        }
    }
}

