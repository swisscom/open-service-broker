package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

@Component
@CompileStatic
@Slf4j
import java.lang.Object
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

    private final String TESTING_SERVICE_INSTANCE_ID = "dummyAsyncServiceBrokerInstanceId"

    private ServiceBrokerServiceProviderRestClient sbspRestClient

    @Autowired
    ServiceBrokerServiceProviderFacade(ServiceBrokerServiceProviderRestClient sbspRestClient) {
        this.sbspRestClient = sbspRestClient
    }

    boolean provisionServiceInstance(ServiceInstance serviceInstance) {
        if(serviceInstance.guid == TESTING_SERVICE_INSTANCE_ID) {
            sbspRestClient = new TestableServiceBrokerServiceProviderRestClient()
        }
        return sbspRestClient.provisionServiceInstance(serviceInstance)

    }

    boolean deprovisionServiceInstance(ServiceInstance serviceInstance) {
        if(serviceInstance.guid == TESTING_SERVICE_INSTANCE_ID)
            sbspRestClient = new TestableServiceBrokerServiceProviderRestClient()
        return sbspRestClient.deprovisionServiceInstance(serviceInstance)
    }

    /*boolean checkServiceDeprovisioningDone(String serviceInstanceId) {
        return sbspRestClient.getServiceInstanceDoesNotExist(serviceInstanceId)
    }*/
}
