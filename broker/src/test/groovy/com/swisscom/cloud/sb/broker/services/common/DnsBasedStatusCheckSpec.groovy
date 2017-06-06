package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.ServiceDetailKey
import com.swisscom.cloud.sb.broker.util.ServiceDetailsHelper
import spock.lang.Specification

class DnsBasedStatusCheckSpec extends Specification {

    DnsBasedStatusCheck statusCheck

    def setup() {
        statusCheck = new DnsBasedStatusCheck()
    }

    def "status is processed correctly for a single resolvable dns"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create().add(ServiceDetailKey.KIBANA_HOST, 'localhost').getDetails())
        expect:
        statusCheck.isReady(serviceInstance)
    }

    def "status is processed correctly for multiple resolvable dns'"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(ServiceDetailKey.KIBANA_HOST, 'localhost')
                .add(ServiceDetailKey.ELASTIC_SEARCH_HOST, 'localhost').getDetails())
        expect:
        statusCheck.isReady(serviceInstance)
    }

    def "status is processed correctly when a dns name is not resolved correctly"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(ServiceDetailKey.KIBANA_HOST, 'localhost')
                .add(ServiceDetailKey.ELASTIC_SEARCH_HOST, 'unknown').getDetails())
        expect:
        !statusCheck.isReady(serviceInstance)
    }

    def "isGone functions correctly for a service instance which consists of hosts all unresolvable"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(ServiceDetailKey.KIBANA_HOST, 'noSuchHost')
                .add(ServiceDetailKey.ELASTIC_SEARCH_HOST, 'ghostHost').getDetails())
        expect:
        statusCheck.isGone(serviceInstance)
    }

    def "isGone functions correctly for a service instance which includes a resolvable host"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(ServiceDetailKey.KIBANA_HOST, 'localhost')
                .add(ServiceDetailKey.ELASTIC_SEARCH_HOST, 'ghostHost').getDetails())
        expect:
        !statusCheck.isGone(serviceInstance)
    }

}
