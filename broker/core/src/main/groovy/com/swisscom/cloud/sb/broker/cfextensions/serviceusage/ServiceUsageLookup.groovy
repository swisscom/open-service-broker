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

package com.swisscom.cloud.sb.broker.cfextensions.serviceusage

import com.google.common.base.Optional
import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.model.usage.ServiceUsage
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ServiceUsageLookup {
    private final ServiceProviderLookup serviceProviderLookup

    @Autowired
    ServiceUsageLookup(ServiceProviderLookup serviceProviderLookup) {
        this.serviceProviderLookup = serviceProviderLookup
    }

    ServiceUsage usage(ServiceInstance instance, Optional<Date> optionalEnddate) {
        Preconditions.checkNotNull(instance, "A valid ServiceInstance is required.")
        return findServiceUsageProvider(instance).findUsage(instance, optionalEnddate)
    }

    private ServiceUsageProvider findServiceUsageProvider(ServiceInstance serviceInstance) {
        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (!(serviceProvider instanceof ServiceUsageProvider)) {
            throw new RuntimeException("Service provider: ${serviceProvider.class.name} does not provide service usage information")
        }
        return serviceProvider as ServiceUsageProvider
    }
}
