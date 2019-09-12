package com.swisscom.cloud.sb.broker.services.inventory

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.springframework.data.util.Pair
import org.springframework.stereotype.Service

@CompileStatic
@Service
class LocalInventoryServiceImpl implements InventoryService {

    private final ServiceInstanceRepository serviceInstanceRepository
    private final ServiceDetailRepository serviceDetailRepository

    LocalInventoryServiceImpl(
            ServiceInstanceRepository serviceInstanceRepository,
            ServiceDetailRepository serviceDetailRepository) {
        this.serviceDetailRepository = serviceDetailRepository
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    private ServiceInstance getServiceInstance(String guid) {
        def serviceInstance = serviceInstanceRepository.findByGuid(guid)

        if (serviceInstance == null) {
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew("No serviceinstance with guid:${guid} found.")
        }

        return serviceInstance
    }

    @Override
    Pair<String, String> get(String serviceInstanceGuid, String key) {
        def details = getServiceInstance(serviceInstanceGuid).details.findAll { d -> d.key.equalsIgnoreCase(key) }
        Preconditions.checkArgument(details.size() > 0, "No details for key:${key} found")
        Preconditions.checkArgument(details.size() == 1, "Multiple details for key:${key} found")

        return Pair.of(details.get(0).key, details.get(0).value)
    }

    @Override
    Pair<String, String> get(String serviceInstanceGuid, String key, String defaultValue) {
        def details = getServiceInstance(serviceInstanceGuid).details.findAll { d -> d.key.equalsIgnoreCase(key) }
        Preconditions.checkArgument(details.size() <= 1, "Multiple details for key:${key} found")

        return Pair.of(key, details.size() == 0 ? defaultValue : details.get(0).value)
    }

    @Override
    List<Pair<String, String>> getAll(String serviceInstanceGuid, String key) {
        return getServiceInstance(serviceInstanceGuid).details
                .findAll { d -> d.key.equalsIgnoreCase(key) }
                .collect { d -> Pair.of(d.key, d.value) }
    }

    @Override
    List<Pair<String, String>> get(String serviceInstanceGuid) {
        return getServiceInstance(serviceInstanceGuid).details
                .findAll { d -> d.key && d.value }
                .collect { d -> Pair.of(d.key, d.value) }
    }

    @Override
    List<Pair<String, String>> set(String serviceInstanceGuid, Pair<String, String> data) {
        def serviceInstance = getServiceInstance(serviceInstanceGuid)

        if (!addOrUpdateReturnTrueIfUpdated(serviceInstance, data)) {
            serviceInstance = serviceInstanceRepository.save(serviceInstance)
        }

        return get(serviceInstanceGuid)
    }

    private boolean addOrUpdateReturnTrueIfUpdated(ServiceInstance serviceInstance, Pair<String, String> data) {
        ServiceDetail detail = serviceInstance.details.find { d -> d.key.equals(data.first) }

        if (detail != null) {
            if (detail.value != data.second) {
                detail.value = data.second
                detail = serviceDetailRepository.save(detail)
            }

            return true
        }

        def serviceDetail = ServiceDetail.from(data.first, data.second)
        serviceDetail = serviceDetailRepository.save(serviceDetail)
        serviceInstance.details.add(serviceDetail)

        return false
    }

    @Override
    List<Pair<String, String>> replace(String serviceInstanceGuid, List<Pair<String, String>> data) {
        def serviceInstance = getServiceInstance(serviceInstanceGuid)

        def previousDetails = serviceInstance.details.toList()

        for (def entry in data) {
            if (addOrUpdateReturnTrueIfUpdated(serviceInstance, entry)) {
                previousDetails.removeAll { d -> d.key.equals(entry.first) }
            }
        }

        for (def entry in previousDetails) {
            serviceInstance.details.removeIf({ d -> d.key.equalsIgnoreCase(entry.key) })
            serviceDetailRepository.delete(entry)
        }

        serviceInstance = serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> append(String serviceInstanceGuid, List<Pair<String, String>> data) {
        def serviceInstance = getServiceInstance(serviceInstanceGuid)

        for (def entry in data) {
            def serviceDetail = ServiceDetail.from(entry.first, entry.second)
            serviceDetail = serviceDetailRepository.save(serviceDetail)
            serviceInstance.details.add(serviceDetail)
        }

        serviceInstance = serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> delete(String serviceInstanceGuid, String key) {
        def serviceInstance = getServiceInstance(serviceInstanceGuid)
        def detail = serviceInstance.details.find { d -> d.key.equalsIgnoreCase(key) }

        if (detail != null) {
            serviceInstance.details.removeAll { d -> d.key.equals(key) }
            serviceDetailRepository.delete(detail)
            serviceInstance = serviceInstanceRepository.save(serviceInstance)
        }

        return get(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> replaceByKey(String serviceInstanceGuid, String key, String[] values) {
        def serviceInstance = getServiceInstance(serviceInstanceGuid)
        List<ServiceDetail> toDelete = serviceInstance.details.findAll { d -> d.key.equalsIgnoreCase(key) }

        values.each {
            v ->
                def match = toDelete.find { d -> d.value == v }
                if (match != null) {
                    toDelete.removeAll { dd -> dd.id == match.id }
                } else {
                    serviceInstance.details.add(ServiceDetail.from(key, v))
                }
        }

        toDelete.each { d ->
            serviceInstance.details.removeAll { dd -> dd.id == d.id }
            serviceDetailRepository.delete(d)
        }
        serviceInstance = serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }
}
