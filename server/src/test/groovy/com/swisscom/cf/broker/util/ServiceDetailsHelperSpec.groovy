package com.swisscom.cf.broker.util

import com.swisscom.cf.broker.model.ServiceDetail
import spock.lang.Specification

class ServiceDetailsHelperSpec extends Specification {

    def "find works correctly when key is *not* found"() {
        expect:
        !ServiceDetailsHelper.from([]).findValue(ServiceDetailKey.HOST).present
    }

    def "find works correctly when key is found"() {
        expect:
        ServiceDetailsHelper.from([ServiceDetail.from(ServiceDetailKey.HOST, 'someValue')]).findValue(ServiceDetailKey.HOST).get() == 'someValue'
    }

}
