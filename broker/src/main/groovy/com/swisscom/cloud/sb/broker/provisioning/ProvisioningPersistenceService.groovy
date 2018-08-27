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

import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.DeprovisionRequest
import com.swisscom.cloud.sb.broker.model.ProvisionRequest
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ApplicationUserRepository
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

    @Autowired
    private ServiceContextPersistenceService contextPersistenceService

    @Autowired
    protected ApplicationUserRepository applicationUserRepository

    def saveProvisionRequest(ProvisionRequest provisionRequest) {
        provisionRequestRepository.save(provisionRequest)
    }

    def saveDeprovisionRequest(DeprovisionRequest deprovisionRequest) {
        deprovisionRequestRepository.save(deprovisionRequest)
    }

    ServiceInstance createServiceInstance(ProvisionRequest provisionRequest) {
        ServiceInstance instance = new ServiceInstance()
        provisionRequest.serviceInstance = instance
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
        ServiceInstance parentInstance = null
        if (!provisionRequest.parameters || !provisionRequest.parameters.contains("parent_reference"))
            return parentInstance;

        parentInstance = findParentServiceInstance(provisionRequest.parameters)
        if (parentInstance == null)
            ErrorCode.SERVICE_INSTANCE_NOT_FOUND.throwNew()

        instance.parentServiceInstance = parentInstance
        serviceInstanceRepository.merge(instance)
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
        ServiceInstance instance = createServiceInstance(provisionRequest)
        instance.completed = !provisionResponse.isAsync

        return updateServiceDetails(provisionResponse.details, instance)
    }

    ServiceInstance updateServiceDetails(final Collection<ServiceDetail> details, final ServiceInstance instance) {
        details?.each {
            ServiceDetail detail ->
                if (detail.isUniqueKey()) {
                    removeExistingServiceDetailsForKey(detail, instance)
                }
                serviceDetailRepository.save(detail)

                if (!instance.details.any { d -> d == detail })
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

    ServiceInstance createServiceInstanceOrUpdateDetails(ProvisionRequest provisionRequest, ProvisionResponse provisionResponse) {
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
        instance.dateDeleted = new Date()
        serviceInstanceRepository.save(instance)
    }

    ServiceInstance getServiceInstance(String guid) {
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

