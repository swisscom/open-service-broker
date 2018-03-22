package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

import static com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper.from

@Component
@CompileStatic
@Slf4j
class ServiceBrokerServiceProviderFacade {

    private final ServiceBrokerServiceProviderClient sbspClient

    @Autowired
    ServiceBrokerServiceProviderFacade(ServiceBrokerServiceProviderClient sbspClient) {
        this.sbspClient = sbspClient
    }

    boolean checkServiceProvisioningDone(String serviceInstanceId) {
        return sbspClient.getServiceInstanceInformation(serviceInstanceId)

    }

    /*boolean checkServiceDeprovisioningDone(String serviceInstanceId) {
        return sbspRestClient.getServiceInstanceDoesNotExist(serviceInstanceId)
    }*/
}
