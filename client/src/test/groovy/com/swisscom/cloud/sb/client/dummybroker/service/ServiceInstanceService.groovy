package com.swisscom.cloud.sb.client.dummybroker.service

import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.*
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ServiceInstanceService implements org.springframework.cloud.servicebroker.service.ServiceInstanceService{

    @Override
    CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
        return new CreateServiceInstanceResponse()
    }

    @Override
    GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
        return new GetLastServiceOperationResponse().withOperationState(OperationState.SUCCEEDED).withDescription('some description')
    }

    @Override
    DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
        return new DeleteServiceInstanceResponse().withAsync(false)
    }

    @Override
    UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
        return new UpdateServiceInstanceResponse().withAsync(false)
    }
}
