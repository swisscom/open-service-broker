/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.updating

import com.fasterxml.jackson.databind.ObjectMapper
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.repository.UpdateRequestRepository
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

    void updatePlanAndServiceDetails(final ServiceInstance serviceInstance, final String updateParameters, final Collection<ServiceDetail> serviceDetails, Plan plan, ServiceContext serviceContext) {
        serviceInstance.plan = plan
        serviceInstance.parameters = mergeServiceInstanceParameter(serviceInstance.parameters, updateParameters)
        if (serviceContext) {
            serviceInstance.serviceContext = serviceContext
        }
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
