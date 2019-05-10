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

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import com.swisscom.cloud.sb.model.endpoint.Endpoint

class EndpointLookup {
    public static final Collection<String> DEFAULT_PROTOCOLS = Collections.unmodifiableCollection(["tcp"])

    Collection<Endpoint> findEndpoints(ServiceInstance serviceInstance, EndpointConfig config) {
        def result = new LinkedList<Endpoint>()
        def portsForServiceInstance = ServiceDetailsHelper.from(serviceInstance).findAllWithServiceDetailType(ServiceDetailType.PORT)
        if (config.protocols.size() == 0){
            config.protocols.addAll(DEFAULT_PROTOCOLS)
        }

        config.protocols.each { String protocol ->
            config.ipRanges.each { String ipRange ->
                portsForServiceInstance.each { String port ->
                        result.add(new Endpoint(protocol: protocol, ports: port, destination: ipRange))
                }
            }
        }
        return result
    }
}
