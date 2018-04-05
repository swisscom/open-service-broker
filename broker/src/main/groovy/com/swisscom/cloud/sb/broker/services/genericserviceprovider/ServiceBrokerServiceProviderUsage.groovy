package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.model.repository.GenericProvisionRequestPlanParameter
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ServiceBrokerServiceProviderUsage {

    @Autowired
    ApplicationUserConfig userConfig

    ServiceUsage findUsage(ServiceInstance serviceInstance) {
        GenericProvisionRequestPlanParameter req = ServiceBrokerServiceProvider.populateGenericProvisionRequestPlanParameter(serviceInstance.plan.parameters)
        ServiceBrokerServiceProviderUsageClient serviceBrokerServiceProviderUsageClient = new ServiceBrokerServiceProviderUsageClient(req.baseUrl, req.username, req.password, userConfig)
        return serviceBrokerServiceProviderUsageClient.getLatestServiceInstanceUsage(serviceInstance.guid)
    }
}
