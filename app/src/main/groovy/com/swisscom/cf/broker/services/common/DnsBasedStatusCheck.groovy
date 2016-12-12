package com.swisscom.cf.broker.services.common

import com.google.common.collect.Sets
import com.swisscom.cf.broker.model.ServiceInstance
import com.swisscom.cf.broker.util.ServiceDetailType
import com.swisscom.cf.broker.util.ServiceDetailsHelper
import groovy.util.logging.Log4j
import groovyx.gpars.GParsPool
import org.springframework.stereotype.Component

@Component
@Log4j
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
