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

package com.swisscom.cloud.sb.client.dummybroker.service

import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ServiceInstanceBindingService implements org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService {
    @Override
    CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        return CreateServiceInstanceAppBindingResponse.builder().build()
    }

    @Override
    DeleteServiceInstanceBindingResponse deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest)
            throws ServiceBrokerException {
        return DeleteServiceInstanceBindingResponse.builder().build()
    }
}
