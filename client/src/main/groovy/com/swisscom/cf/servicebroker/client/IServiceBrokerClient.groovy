package com.swisscom.cf.servicebroker.client

import com.swisscom.cf.servicebroker.client.model.DeleteServiceInstanceBindingRequest
import com.swisscom.cf.servicebroker.client.model.DeleteServiceInstanceRequest
import com.swisscom.cf.servicebroker.client.model.LastOperationResponse
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.*
import org.springframework.http.ResponseEntity

@CompileStatic
interface IServiceBrokerClient {
    ResponseEntity<Catalog> getCatalog()
    ResponseEntity<LastOperationResponse> getServiceInstanceLastOperation(String serviceInstanceId)
    ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request)
    ResponseEntity<UpdateServiceInstanceResponse> updateServiceInstance(UpdateServiceInstanceRequest request)
    ResponseEntity<Void> deleteServiceInstance(DeleteServiceInstanceRequest request)
        ResponseEntity<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
    ResponseEntity<Void> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request)
}
