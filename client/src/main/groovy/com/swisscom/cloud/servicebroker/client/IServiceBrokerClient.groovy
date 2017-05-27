package com.swisscom.cloud.servicebroker.client

import com.swisscom.cloud.servicebroker.client.model.LastOperationResponse
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.*
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClient {
    ResponseEntity<Catalog> getCatalog()
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId)
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request)
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request)
    ResponseEntity<Void> deleteServiceInstance(com.swisscom.cloud.servicebroker.client.model.DeleteServiceInstanceRequest request)
        ResponseEntity<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
    ResponseEntity<Void> deleteServiceInstanceBinding(com.swisscom.cloud.servicebroker.client.model.DeleteServiceInstanceBindingRequest request)
}
