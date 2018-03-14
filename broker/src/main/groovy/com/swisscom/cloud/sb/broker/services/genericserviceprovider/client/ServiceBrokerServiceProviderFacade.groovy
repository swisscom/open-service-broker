package com.swisscom.cloud.sb.broker.services.genericserviceprovider.client

@Component
@CompileStatic
@Slf4j
import java.lang.Object

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
