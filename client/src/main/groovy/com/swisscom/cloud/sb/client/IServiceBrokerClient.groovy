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

package com.swisscom.cloud.sb.client

import com.swisscom.cloud.sb.client.model.LastOperationResponse
import com.swisscom.cloud.sb.client.model.ServiceInstanceBindingResponse
import com.swisscom.cloud.sb.client.model.ServiceInstanceResponse
import com.swisscom.cloud.sb.client.model.ProvisionResponseDto
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.Catalog
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClient {
    ResponseEntity<Catalog> getCatalog()
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId)
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId, String operationId)
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request)
    ResponseEntity<ProvisionResponseDto> provision(CreateServiceInstanceRequest request)
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request)
    ResponseEntity<Void> deleteServiceInstance(com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest request)
    ResponseEntity<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
    ResponseEntity<Void> deleteServiceInstanceBinding(com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest request)
    ResponseEntity<ServiceInstanceResponse> getServiceInstance(String serviceInstanceId)
    ResponseEntity<ServiceInstanceBindingResponse> getServiceInstanceBinding(String serviceInstanceId, String bindingId)
}
