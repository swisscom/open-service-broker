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

package com.swisscom.cloud.sb.broker.cfextensions.endpoint

import com.google.common.base.Preconditions
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.services.common.ServiceProviderLookup
import com.swisscom.cloud.sb.model.endpoint.Endpoint
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@CompileStatic
@Service
class EndpointService {
    @Autowired
    protected ServiceProviderLookup serviceProviderLookup

    Collection<Endpoint> lookup(ServiceInstance serviceInstance) {
        Preconditions.checkNotNull(serviceInstance, "A valid ServiceInstance argument is required")
        Preconditions.checkNotNull(serviceInstance.plan, "A valid plan on ServiceInstance argument is required")

        def serviceProvider = serviceProviderLookup.findServiceProvider(serviceInstance.plan)
        if (serviceProvider instanceof EndpointProvider) {
            return serviceProvider.findEndpoints(serviceInstance)
        } else {
            return []
        }
    }
}
