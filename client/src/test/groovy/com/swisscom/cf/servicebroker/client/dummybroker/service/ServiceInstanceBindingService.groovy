package com.swisscom.cf.servicebroker.client.dummybroker.service

import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest
import org.springframework.stereotype.Service

@Service
@CompileStatic
class ServiceInstanceBindingService implements org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService {
    @Override
    CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest) throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        return new CreateServiceInstanceBindingResponse()
    }

    @Override
    void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest) throws ServiceBrokerException {
    }
}
