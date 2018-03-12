package com.swisscom.cloud.sb.broker.updating

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.UpdateRequest
import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.model.repository.UpdateRequestRepository
import com.swisscom.cloud.sb.broker.provisioning.ProvisioningPersistenceService
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@CompileStatic
@Slf4j
@Service
@Transactional
class UpdatingPersistenceService {
    private UpdateRequestRepository updateRequestRepository
    private ServiceInstanceRepository serviceInstanceRepository
    private ProvisioningPersistenceService provisioningPersistenceService

    @Autowired
    UpdatingPersistenceService(
            UpdateRequestRepository updateRequestRepository,
            ServiceInstanceRepository serviceInstanceRepository,
            ProvisioningPersistenceService provisioningPersistenceService) {
        this.updateRequestRepository = updateRequestRepository
        this.serviceInstanceRepository = serviceInstanceRepository
        this.provisioningPersistenceService = provisioningPersistenceService
    }

    void saveUpdateRequest(UpdateRequest updateRequest) {
        updateRequestRepository.saveAndFlush(updateRequest)
    }

    void updatePlanAndServiceDetails(final ServiceInstance serviceInstance, final String updateParameters, final Collection<ServiceDetail> serviceDetails, Plan plan) {
        serviceInstance.plan = plan
        serviceInstance.parameters = mergeServiceInstanceParameter(serviceInstance.parameters, updateParameters)
        serviceInstanceRepository.saveAndFlush(serviceInstance)
        updateServiceDetails(serviceDetails, serviceInstance)
    }

    private void updateServiceDetails(final Collection<ServiceDetail> serviceDetails, final ServiceInstance serviceInstance) {
        provisioningPersistenceService.updateServiceDetails(serviceDetails, serviceInstance)
    }

    String mergeServiceInstanceParameter(String oldParameters, String updateParamters) {
        def startMap = toMap(oldParameters)
        def updateMap = toMap(updateParamters)

        updateMap.each({ key, value -> startMap.put(key, value) })

        return serialize(startMap)
    }

    String serialize(Object object) {
        if (!object) return null
        return new ObjectMapper().writeValueAsString(object)
    }

    Map toMap(String jsonMap) {
        if (StringUtils.isEmpty(jsonMap))
            return new HashMap()
        def result = new JsonSlurper().parseText(jsonMap)
        return (Map)result
    }
}
