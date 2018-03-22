package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.repository.ServiceInstanceRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ServiceBrokerServiceProviderClient {

    @Autowired
    private ServiceInstanceRepository serviceInstanceRepository

    boolean getServiceInstanceInformation(String serviceInstanceId) {
        serviceInstanceRepository.findByGuid(serviceInstanceId).completed
        return true
    }
}
