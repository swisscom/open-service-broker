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

package com.swisscom.cloud.sb.broker.services.common


import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.repository.ServiceInstanceRepository
import com.swisscom.cloud.sb.broker.services.ServiceProviderLookup
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper

import java.lang.reflect.ParameterizedType

import static com.swisscom.cloud.sb.broker.services.ServiceProviderLookup.findInternalName

abstract class FreePortFinder<T extends ServiceProvider> {
    private final String portRange
    private final ServiceInstanceRepository serviceInstanceRepository

    FreePortFinder(String portRange, ServiceInstanceRepository serviceInstanceRepository) {
        this.portRange = portRange
        this.serviceInstanceRepository = serviceInstanceRepository
    }

    List<Integer> findFreePorts(int count = 1) {
        if (portRange.contains('-')) {
            int startRange = portRange.substring(0, portRange.indexOf('-')) as int
            int endRange = portRange.substring(portRange.indexOf('-') + 1) as int
            def searchSpace = (startRange..<endRange).toList()
            searchSpace = searchSpace - alreadyReservedPorts()
            Collections.shuffle(searchSpace)
            if (searchSpace.size() == 0 || count > searchSpace.size()) {
                throw new RuntimeException('There are no free ports left!')
            }
            return searchSpace.take(count)
        } else {
            throw new RuntimeException('PortRange should be in format:"n1-n2" where n2>n1 e.g. "27000-45000"')
        }
    }

    private List<Integer> alreadyReservedPorts() {
        List<ServiceInstance> serviceInstances = findServiceInstancesOfServiceProvider()

        return serviceInstances?.collect { ServiceInstance si ->
            Optional<String> optionalPort = ServiceDetailsHelper.from(si.details).findValue(ServiceDetailKey.PORT)
            return optionalPort.present ? optionalPort.get() as int : 0
        }
    }

    private List<ServiceInstance> findServiceInstancesOfServiceProvider() {
        return serviceInstanceRepository.listAllForInternalName(findInternalName(findGenericType()))
    }

    private Class findGenericType() {
        return ((Class) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0])
    }
}
