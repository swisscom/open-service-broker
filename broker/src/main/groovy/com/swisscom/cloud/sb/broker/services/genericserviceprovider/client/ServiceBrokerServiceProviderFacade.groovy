package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

import static com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper.from

class ServiceBrokerServiceProviderFacade {

    private final ServiceBrokerServiceProviderRestClient sbspRestClient

    @Autowired
    ServiceBrokerServiceProviderFacade(ServiceBrokerServiceProviderRestClient sbspRestClient) {
        this.sbspRestClient = sbspRestClient
    }

    boolean checkServiceProvisioningDone(String serviceInstanceId) {
        return sbspRestClient.getServiceInstanceInformation(serviceInstanceId)

    }
}
