package com.swisscom.cloud.sb.broker.services.inventory

import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceDetailRepository
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.util.Pair
import org.springframework.stereotype.Service

import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkState
import static org.apache.commons.lang.StringUtils.isNotBlank

@CompileStatic
@Service
class LocalInventoryServiceImpl implements InventoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalInventoryServiceImpl.class)

    public static String ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED = "service instance guid cannot be null or empty"
    public static String ERROR_KEY_NOT_DEFINED = "key cannot be null or empty"
    public static String ERROR_SERVICE_INSTANCE_NOT_FOUND = "service instance with id:%s not found"
    public static String ERROR_DETAIL_NOT_FOUND = "no details for key:%s found"
    public static String ERROR_DETAIL_NOT_UNIQUE = "multiple details for key:%s found"
    public static String ERROR_DETAIL_MANDATORY = "details are mandatory"
    public static String ERROR_DETAIL_BLANK = "detail key may not be blank"

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
        checkState(serviceInstance != null, ERROR_SERVICE_INSTANCE_NOT_FOUND, guid)

        return serviceInstance
    }

    @Override
    boolean has(String serviceInstanceGuid, String key) {
        LOGGER.debug("has({}, {})", serviceInstanceGuid, key)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(isNotBlank(key), ERROR_KEY_NOT_DEFINED)

        return getServiceInstance(serviceInstanceGuid).details.findAll { d -> d.key.equalsIgnoreCase(key) }.size() > 0
    }

    @Override
    Pair<String, String> get(String serviceInstanceGuid, String key) {
        LOGGER.debug("get({}, {})", serviceInstanceGuid, key)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(isNotBlank(key), ERROR_KEY_NOT_DEFINED)

        def details = getServiceInstance(serviceInstanceGuid).details.findAll { d -> d.key.equalsIgnoreCase(key) }
        checkState(details.size() > 0, ERROR_DETAIL_NOT_FOUND, key)
        checkState(details.size() == 1, ERROR_DETAIL_NOT_UNIQUE, key)

        return Pair.of(details.get(0).key, details.get(0).value)
    }

    @Override
    Pair<String, String> get(String serviceInstanceGuid, String key, String defaultValue) {
        LOGGER.debug("get({}, {}, default:{})", serviceInstanceGuid, key, defaultValue)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(isNotBlank(key), ERROR_KEY_NOT_DEFINED)

        def details = getServiceInstance(serviceInstanceGuid).details.findAll { d -> d.key.equalsIgnoreCase(key) }
        checkState(details.size() <= 1, ERROR_DETAIL_NOT_UNIQUE, key)

        return Pair.of(key, details.size() == 0 ? defaultValue : details.get(0).value)
    }

    @Override
    List<Pair<String, String>> getAll(String serviceInstanceGuid, String key) {
        LOGGER.debug("getAll({}, {})", serviceInstanceGuid, key)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(isNotBlank(key), ERROR_KEY_NOT_DEFINED)

        return getServiceInstance(serviceInstanceGuid).details
                .findAll { d -> d.key.equalsIgnoreCase(key) }
                .collect { d -> Pair.of(d.key, d.value) }
    }

    @Override
    List<Pair<String, String>> get(String serviceInstanceGuid) {
        LOGGER.debug("get({})", serviceInstanceGuid)
        return getAll(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> getAll(String serviceInstanceGuid) {
        LOGGER.debug("getAll({})", serviceInstanceGuid)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)

        return getServiceInstance(serviceInstanceGuid).details
                .findAll { d -> d.key && d.value }
                .collect { d -> Pair.of(d.key, d.value) }
    }

    @Override
    List<Pair<String, String>> set(String serviceInstanceGuid, Pair<String, String> data) {
        LOGGER.debug("set({}, {})", serviceInstanceGuid, data)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(data != null, ERROR_DETAIL_MANDATORY)
        checkArgument(isNotBlank(data.first), ERROR_DETAIL_BLANK)

        def serviceInstance = getServiceInstance(serviceInstanceGuid)
        addOrUpdateReturnTrueIfUpdated(serviceInstance, data)
        serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }

    private boolean addOrUpdateReturnTrueIfUpdated(ServiceInstance serviceInstance, Pair<String, String> data) {
        LOGGER.debug("addOrUpdateReturnTrueIfUpdated({}, {})", serviceInstance, data)
        ServiceDetail detail = serviceInstance.details.find { d -> d.key.equals(data.first) }

        if (detail != null) {
            if (detail.value != data.second) {
                detail.value = data.second
            }

            return true
        }

        def serviceDetail = ServiceDetail.from(data.first, data.second)
        serviceInstance.details.add(serviceDetail)

        return false
    }

    @Override
    List<Pair<String, String>> replace(String serviceInstanceGuid, List<Pair<String, String>> data) {
        LOGGER.debug("replace({}, {})", serviceInstanceGuid, data)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(data != null, ERROR_DETAIL_MANDATORY)
        checkArgument(data.size() == 0 || data.any { kvp -> isNotBlank(kvp.first) }, ERROR_DETAIL_BLANK)

        def serviceInstance = getServiceInstance(serviceInstanceGuid)
        def previousDetails = serviceInstance.details.toList()

        for (def entry in data) {
            if (addOrUpdateReturnTrueIfUpdated(serviceInstance, entry)) {
                previousDetails.removeAll { d -> d.key.equals(entry.first) }
            }
        }

        for (def entry in previousDetails) {
            serviceInstance.details.removeIf({ d -> d.key.equalsIgnoreCase(entry.key) })
        }

        serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> append(String serviceInstanceGuid, List<Pair<String, String>> data) {
        LOGGER.debug("append({}, {})", serviceInstanceGuid, data)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(data != null, ERROR_DETAIL_MANDATORY)
        checkArgument(data.any { kvp -> isNotBlank(kvp.first) }, ERROR_DETAIL_BLANK)

        def serviceInstance = getServiceInstance(serviceInstanceGuid)

        for (def entry in data) {
            serviceInstance.details.add(ServiceDetail.from(entry.first, entry.second))
        }

        serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> delete(String serviceInstanceGuid, String key) {
        LOGGER.debug("delete({}, {})", serviceInstanceGuid, key)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(isNotBlank(key), ERROR_KEY_NOT_DEFINED)

        def serviceInstance = getServiceInstance(serviceInstanceGuid)
        def detail = serviceInstance.details.find { d -> d.key.equalsIgnoreCase(key) }

        if (detail != null) {
            serviceInstance.details.removeAll { d -> d.key.equals(key) }
            serviceInstance = serviceInstanceRepository.save(serviceInstance)
        }

        return get(serviceInstanceGuid)
    }

    @Override
    List<Pair<String, String>> createOrReplaceByKey(String serviceInstanceGuid, String key, String[] values) {
        LOGGER.debug("createOrReplaceByKey({}, {}, {})", serviceInstanceGuid, key, values)
        checkArgument(isNotBlank(serviceInstanceGuid), ERROR_SERVICE_INSTANCE_ID_NOT_DEFINED)
        checkArgument(isNotBlank(key), ERROR_KEY_NOT_DEFINED)
        checkArgument(values != null, ERROR_DETAIL_MANDATORY)

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
        }
        serviceInstanceRepository.save(serviceInstance)

        return get(serviceInstanceGuid)
    }
}
