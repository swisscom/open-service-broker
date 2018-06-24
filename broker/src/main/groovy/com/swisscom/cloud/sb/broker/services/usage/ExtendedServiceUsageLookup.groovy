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

package com.swisscom.cloud.sb.broker.services.usage

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProvider
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.model.usage.extended.ServiceUsageItem
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
@Slf4j
class ExtendedServiceUsageLookup {
    private final ServiceProviderLookup serviceProviderLookup

    @Autowired
    ExtendedServiceUsageLookup(ServiceProviderLookup serviceProviderLookup) {
        this.serviceProviderLookup = serviceProviderLookup
    }

    Set<ServiceUsageItem> getUsage(ServiceInstance serviceInstance) {
        Preconditions.checkNotNull(serviceInstance, "A valid ServiceInstance is required.")

        ServiceProvider serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (!(serviceProvider instanceof ExtendedServiceUsageProvider)) {
            log.info("Usage requested for serviceinstance(guid:${serviceInstance.guid}) " +
                        "with plan(guid:${serviceInstance.plan.guid},name:${serviceInstance.plan.name}) " +
                        "not providing a ExtendedServiceUsageProvider implementation.")

            return new HashSet<ServiceUsageItem>()
        }

        (serviceProvider as ExtendedServiceUsageProvider).getUsages(serviceInstance)
    }
}
