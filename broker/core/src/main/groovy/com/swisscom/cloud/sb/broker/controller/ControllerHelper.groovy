package com.swisscom.cloud.sb.broker.controller

import com.swisscom.cloud.sb.broker.error.ErrorCode
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ControllerHelper {
    private ServiceInstanceRepository serviceInstanceRepository

    ControllerHelper(ServiceInstanceRepository serviceInstanceRepository) {
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    public ServiceInstance getAndCheckServiceInstance(String serviceInstanceId) {
        ServiceInstance serviceInstance = serviceInstanceRepository.findByGuid(serviceInstanceId)
        if (!serviceInstance) {
            ErrorCode.SERVICE_INSTANCE_GONE.throwNew("ID = " + serviceInstanceId)
        }
        if (serviceInstance.deleted) {
            ErrorCode.SERVICE_INSTANCE_DELETED.throwNew("ID = " + serviceInstanceId)
        }
        return serviceInstance
    }
}
