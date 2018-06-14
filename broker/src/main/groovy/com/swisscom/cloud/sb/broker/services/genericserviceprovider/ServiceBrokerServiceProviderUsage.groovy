/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.services.genericserviceprovider

import com.swisscom.cloud.sb.broker.config.ApplicationUserConfig
import com.swisscom.cloud.sb.broker.model.ServiceInstance
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
    ServiceBrokerServiceProviderUsage(ApplicationUserConfig applicationUserConfig) {
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