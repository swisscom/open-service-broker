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

package com.swisscom.cloud.sb.broker.binding

import com.swisscom.cloud.sb.broker.context.ServiceContextPersistenceService
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import com.swisscom.cloud.sb.broker.model.ServiceDetail
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.ApplicationUserRepository
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

    @Autowired
    protected ApplicationUserRepository applicationUserRepository

    @Autowired
    protected CredentialService credentialService

    ServiceBinding create(ServiceInstance serviceInstance, String credentials, String parameters, String guid, Collection<ServiceDetail> details, Context context, String applicationUser) {
        ServiceBinding serviceBinding = new ServiceBinding()
        serviceBinding.guid = guid
        serviceBinding.parameters = parameters
        serviceBinding.applicationUser = applicationUserRepository.findByUsername(applicationUser)
        credentialService.writeCredential(serviceBinding, credentials)
        serviceBindingRepository.save(serviceBinding)
        details?.each {
            ServiceDetail detail ->
                serviceDetailRepository.save(detail)
                serviceBinding.details.add(detail)
        }
        if (context) {
            serviceBinding.serviceContext = contextPersistenceService.findOrCreate(context, serviceInstance.guid)
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
        credentialService.deleteCredential(serviceBinding)
    }

}
