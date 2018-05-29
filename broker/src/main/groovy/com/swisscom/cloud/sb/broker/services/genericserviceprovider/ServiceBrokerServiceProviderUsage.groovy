package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.client.ServiceBrokerClientExtended
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ServiceBrokerServiceProviderUsage {

    ServiceBrokerClientExtended serviceBrokerClientExtended

    ApplicationUserConfig userConfig

    @Autowired
    ServiceBrokerServiceProviderUsage(ApplicationUserConfig applicationUserConfig){
        this.userConfig = applicationUserConfig
        this.serviceBrokerClientExtended = null
    }

    ServiceBrokerServiceProviderUsage(ApplicationUserConfig userConfig, ServiceBrokerClientExtended serviceBrokerClientExtended) {
        this.userConfig = userConfig
        this.serviceBrokerClientExtended = serviceBrokerClientExtended
    }

    ServiceUsage findUsage(ServiceInstance serviceInstance, Optional<Date> enddate) {
        GenericProvisionRequestPlanParameter req = ServiceBrokerServiceProvider.populateGenericProvisionRequestPlanParameter(serviceInstance.plan.parameters)
        ServiceBrokerServiceProviderUsageClient serviceBrokerServiceProviderUsageClient = instantiateServiceBrokerServiceProviderUsageClient(req)
        return serviceBrokerServiceProviderUsageClient.getLatestServiceInstanceUsage(serviceInstance.guid)
    }

    ServiceBrokerServiceProviderUsageClient instantiateServiceBrokerServiceProviderUsageClient(GenericProvisionRequestPlanParameter req) {
        if (serviceBrokerClientExtended == null) {
            return new ServiceBrokerServiceProviderUsageClient(req.baseUrl, req.username, req.password, userConfig)
        } else {
            return new ServiceBrokerServiceProviderUsageClient(req.baseUrl, req.username, req.password, userConfig, serviceBrokerClientExtended)
        }
    }
}
