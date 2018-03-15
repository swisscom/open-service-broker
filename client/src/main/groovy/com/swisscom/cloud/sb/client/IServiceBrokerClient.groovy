package com.swisscom.cloud.sb.client

import com.swisscom.cloud.sb.client.model.LastOperationResponse
import com.swisscom.cloud.sb.client.model.ServiceInstanceBindingResponse
import com.swisscom.cloud.sb.client.model.ServiceInstanceResponse
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
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request)
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request)
    ResponseEntity<Void> deleteServiceInstance(com.swisscom.cloud.sb.client.model.DeleteServiceInstanceRequest request)
    ResponseEntity<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
    ResponseEntity<Void> deleteServiceInstanceBinding(com.swisscom.cloud.sb.client.model.DeleteServiceInstanceBindingRequest request)
    ResponseEntity<ServiceInstanceResponse> getServiceInstance(String serviceInstanceId)
    ResponseEntity<ServiceInstanceBindingResponse> getServiceInstanceBinding(String serviceInstanceId, String bindingId)
}
