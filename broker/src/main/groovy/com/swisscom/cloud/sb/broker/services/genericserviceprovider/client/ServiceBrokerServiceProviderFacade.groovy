package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ServiceBrokerServiceProviderFacade {

    private final String TESTING_SERVICE_INSTANCE_ID = "asyncServiceBrokerServiceProviderInstanceId"

    private ServiceBrokerServiceProviderRestClient sbspRestClient

    @Autowired
    ServiceBrokerServiceProviderFacade(ServiceBrokerServiceProviderRestClient sbspRestClient) {
        this.sbspRestClient = sbspRestClient
    }

    boolean provisionServiceInstance(ServiceInstance serviceInstance) {
        if (serviceInstance.guid == TESTING_SERVICE_INSTANCE_ID) {
            sbspRestClient = new TestableServiceBrokerServiceProviderRestClient()
        }
        return sbspRestClient.provisionServiceInstance(serviceInstance)

    }

    boolean deprovisionServiceInstance(ServiceInstance serviceInstance) {
        if (serviceInstance.guid == TESTING_SERVICE_INSTANCE_ID)
            sbspRestClient = new TestableServiceBrokerServiceProviderRestClient()
        return sbspRestClient.deprovisionServiceInstance(serviceInstance)
    }
}
