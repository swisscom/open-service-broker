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

package com.swisscom.cloud.sb.broker.provisioning


import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.*
import com.swisscom.cloud.sb.broker.repository.*
import com.swisscom.cloud.sb.broker.updating.UpdateResponse
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
@Transactional
@CompileStatic
class ProvisioningPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisioningPersistenceService.class)

    @Autowired
    private ProvisionRequestRepository provisionRequestRepository

    @Autowired
    private DeprovisionRequestRepository deprovisionRequestRepository

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    @Autowired
    private ServiceDetailRepository serviceDetailRepository

    @Autowired
    protected ApplicationUserRepository applicationUserRepository

    def saveProvisionRequest(ProvisionRequest provisionRequest) {
        LOGGER.debug("saveProvisionRequest({})", provisionRequest)
        provisionRequestRepository.save(provisionRequest)
    }

    def saveDeprovisionRequest(DeprovisionRequest deprovisionRequest) {
        LOGGER.debug("saveDeprovisionRequest({})", deprovisionRequest)
        deprovisionRequestRepository.save(deprovisionRequest)
    }

    ServiceInstance createServiceInstance(ProvisionRequest provisionRequest) {
        LOGGER.debug("createServiceInstance({})", provisionRequest)
        ServiceInstance instance = new ServiceInstance()
        instance.guid = provisionRequest.serviceInstanceGuid
        instance.plan = provisionRequest.plan
        instance.parameters = provisionRequest.parameters
        instance.serviceContext = provisionRequest.serviceContext
        instance.applicationUser = applicationUserRepository.findByUsername(provisionRequest.applicationUser)
        serviceInstanceRepository.save(instance)

        // set parent service instance if specified
        setParentServiceInstance(provisionRequest, instance)

        return instance
    }

    private ServiceInstance setParentServiceInstance(ProvisionRequest provisionRequest, ServiceInstance instance) {
        LOGGER.debug("setParentServiceInstance({}, {})", provisionRequest, instance)
        ServiceInstance parentInstance = null
        if (!provisionRequest.parameters || !provisionRequest.parameters.contains("parent_reference"))
            return parentInstance;

        parentInstance = findParentServiceInstance(provisionRequest.parameters)
        if (parentInstance == null)
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()

        instance.parentServiceInstance = parentInstance
        parentInstance.childs << instance
        serviceInstanceRepository.merge(parentInstance)

        parentInstance
    }

    /**
     * Search for a parent ServiceInstance by 'service_instance_guid'.
     * @param parameters as a JSON string
     * @return Parent ServiceInstance if found, otherwise null
     */
    ServiceInstance findParentServiceInstance(String parameters) {
        LOGGER.debug("findParentServiceInstance({})", parameters)
        ServiceInstance parentInstance = null
        def jsonSlurper = new JsonSlurper()
        def parametersMap = jsonSlurper.parseText(parameters) as Map
        def parentReference = parametersMap?.parent_reference as String
        if (parentReference) {
            parentInstance = serviceInstanceRepository.findByGuid(parentReference)
        }
        parentInstance
    }

    ServiceInstance createServiceInstance(ProvisionRequest provisionRequest, ProvisionResponse provisionResponse) {
        LOGGER.debug("createServiceInstance({}, {})", provisionRequest, provisionResponse)
        ServiceInstance instance = createServiceInstance(provisionRequest)
        instance.completed = !provisionResponse.isAsync

        return updateServiceDetails(provisionResponse.details, instance)
    }

    ServiceInstance updateServiceDetails(final Collection<ServiceDetail> details, final ServiceInstance instance) {
        LOGGER.debug("updateServiceDetails({}, {})", details, instance)

        if (details == null) {
            return instance
        }

        def tmpServiceInstance = getServiceInstance(instance.guid)
        details.each {
            ServiceDetail detail ->
                if (tmpServiceInstance.details.any { d -> d.equals(detail) } && detail.isUniqueKey()) {
                    def existingServiceDetail = tmpServiceInstance.details.find { d -> d.equals(detail) }
                    existingServiceDetail.value = detail.value
                    existingServiceDetail.uniqueKey = detail.uniqueKey
                } else {
                    tmpServiceInstance.details.add(detail)
                }
        }

        return serviceInstanceRepository.saveAndFlush(tmpServiceInstance)
    }

    ServiceInstance createServiceInstanceOrUpdateDetails(ProvisionRequest provisionRequest, ProvisionResponse provisionResponse) {
        LOGGER.debug("createServiceInstanceOrUpdateDetails({}, {})", provisionRequest, provisionResponse)
        def serviceInstance = getServiceInstance(provisionRequest.serviceInstanceGuid)
        if (serviceInstance) {
            return updateServiceDetails(provisionResponse.details, serviceInstance)
        } else {
            return createServiceInstance(provisionRequest, provisionResponse)
        }
    }

    ServiceInstance updateServiceDetails(UpdateRequest updateRequest, UpdateResponse updateResponse) {
        LOGGER.debug("updateServiceDetails({}, {})", updateRequest, updateResponse)
        def serviceInstance = getServiceInstance(updateRequest.serviceInstanceGuid)
        if (serviceInstance) {
            return updateServiceDetails(updateResponse.details, serviceInstance)
        }
    }

    ServiceInstance updateServiceInstanceCompletion(ServiceInstance instance, boolean completed) {
        LOGGER.debug("updateServiceInstanceCompletion({}, {})", instance, completed)
        def tmpInstance = getServiceInstance(instance.guid)
        tmpInstance.completed = completed
        return serviceInstanceRepository.saveAndFlush(tmpInstance)
    }

    ServiceInstance markServiceInstanceAsDeleted(ServiceInstance instance) {
        LOGGER.debug("markServiceInstanceAsDeleted({})", instance)
        instance.deleted = true
        instance.dateDeleted = new Date()
        return serviceInstanceRepository.save(instance)
    }

    ServiceInstance getServiceInstance(String guid) {
        LOGGER.debug("getServiceInstance({})", guid)
        return serviceInstanceRepository.findByGuid(guid)
    }

    ServiceInstance deleteServiceInstance(ServiceInstance serviceInstance) {
        LOGGER.debug("deleteServiceInstance({})", serviceInstance)
        Set<ServiceDetail> details = new HashSet<ServiceDetail>(serviceInstance.details ?: new ArrayList<ServiceDetail>())
        details.each {
            ServiceDetail detail ->
                serviceInstance.details.remove(detail)
                serviceDetailRepository.delete(detail)
        }

        return serviceInstanceRepository.delete(serviceInstance)
    }

    ServiceInstance deleteServiceInstanceAndCorrespondingDeprovisionRequestIfExists(ServiceInstance serviceInstance) {
        LOGGER.debug("deleteServiceInstanceAndCorrespondingDeprovisionRequestIfExists({})", serviceInstance)
        removeDeprovisionRequestIfExists(serviceInstance.guid)

        return deleteServiceInstance(serviceInstance)
    }

    def removeProvisionRequestIfExists(String guid) {
        LOGGER.debug("removeProvisionRequestIfExists({})", guid)
        ProvisionRequest provisionRequest = provisionRequestRepository.findByServiceInstanceGuid(guid)
        if (provisionRequest) {
            log.info("Deleting provisionRequest with id:${guid}")
            provisionRequestRepository.delete(provisionRequest)
        } else {
            log.info("Found no provisionRequest with id:${guid} to delete")
        }
    }

    def removeDeprovisionRequestIfExists(String guid) {
        LOGGER.debug("removeDeprovisionRequestIfExists({})", guid)
        DeprovisionRequest deprovisionRequest = deprovisionRequestRepository.findByServiceInstanceGuid(guid)
        if (deprovisionRequest) {
            log.info("Deleting deprovisionRequest(s) with id:${guid}")
            deprovisionRequestRepository.delete(deprovisionRequest)
        } else {
            log.info("Found no deprovisionRequest with id:${guid} to delete")
        }
    }
}

