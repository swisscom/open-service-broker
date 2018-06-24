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

import com.google.common.collect.Sets
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.springframework.stereotype.Component

@Component
@Slf4j
class DnsBasedStatusCheck {
    boolean isReady(ServiceInstance serviceInstance) {
        return isServiceReady(hosts(serviceInstance))
    }

    boolean isGone(ServiceInstance serviceInstance) {
        return areAllHostsGone(hosts(serviceInstance))
    }

    private static Collection<String> hosts(ServiceInstance serviceInstance) {
        return ServiceDetailsHelper.from(serviceInstance.details).findAllWithServiceDetailType(ServiceDetailType.HOST)
    }

    private boolean isServiceReady(final Collection<String> hosts) {
        Set results = areHostsResolvable(hosts)
        return !results.contains(false)
    }

    private Set areHostsResolvable(hosts) {
        def results = Sets.newConcurrentHashSet()
        GParsPool.withPool(3) {
            hosts.each {
                results.add(isHostResolvable(it))
            }
        }
        return results
    }

    private boolean areAllHostsGone(final Collection<String> hosts) {
        Set results = areHostsResolvable(hosts)
        return !results.contains(true)
    }

    public static boolean isHostResolvable(String host) {
        try {
            InetAddress.getByName(host)
            return true
        } catch (UnknownHostException e) {
            log.info("Host name ${host} not resolved!")
            return false
        }
    }

    public List<String> resolveAll(String host) {
        try {
            Arrays.asList(InetAddress.getAllByName(host))
        } catch (UnknownHostException e) {
            log.info("Host name ${host} not resolved!")
            return []
        }
    }
}
