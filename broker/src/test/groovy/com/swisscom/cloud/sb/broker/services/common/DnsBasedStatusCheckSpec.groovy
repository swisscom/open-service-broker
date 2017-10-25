package com.swisscom.cloud.sb.broker.services.common

import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.util.servicedetail.AbstractServiceDetailKey
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailType
import com.swisscom.cloud.sb.broker.util.servicedetail.ServiceDetailsHelper
import spock.lang.Specification

class DnsBasedStatusCheckSpec extends Specification {

    DnsBasedStatusCheck statusCheck

    def setup() {
        statusCheck = new DnsBasedStatusCheck()
    }

    def "status is processed correctly for a single resolvable dns"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create().add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST1, 'localhost').getDetails())
        expect:
        statusCheck.isReady(serviceInstance)
    }

    def "status is processed correctly for multiple resolvable dns'"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST1, 'localhost')
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST2, 'localhost').getDetails())
        expect:
        statusCheck.isReady(serviceInstance)
    }

    def "status is processed correctly when a dns name is not resolved correctly"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST1, 'localhost')
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST2, 'unknown').getDetails())
        expect:
        !statusCheck.isReady(serviceInstance)
    }

    def "isGone functions correctly for a service instance which consists of hosts all unresolvable"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST1, 'noSuchHost')
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST2, 'ghostHost').getDetails())
        expect:
        statusCheck.isGone(serviceInstance)
    }

    def "isGone functions correctly for a service instance which includes a resolvable host"() {
        given:
        ServiceInstance serviceInstance = new ServiceInstance(details: ServiceDetailsHelper.create()
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST1, 'localhost')
                .add(DnsBasedStatusCheckServiceDetailKey.TEST_HOST2, 'ghostHost').getDetails())
        expect:
        !statusCheck.isGone(serviceInstance)
    }

    enum DnsBasedStatusCheckServiceDetailKey implements AbstractServiceDetailKey{

        TEST_HOST1("dns_based_status_check_test_host1", ServiceDetailType.HOST),
        TEST_HOST2("dns_based_status_check_test_host2", ServiceDetailType.HOST)

        DnsBasedStatusCheckServiceDetailKey(String key, ServiceDetailType serviceDetailType) {
            com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__key = key
            com_swisscom_cloud_sb_broker_util_servicedetail_AbstractServiceDetailKey__serviceDetailType = serviceDetailType
        }
    }


}
