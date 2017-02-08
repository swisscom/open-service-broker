package com.swisscom.cf.broker.functional

import com.swisscom.cf.broker.error.ErrorCode

class ServiceUpdateFunctionalSpec extends BaseFunctionalSpec {

    def "service updates are not allowed"() {
        when:
        def response = serviceLifeCycler.requestUpdateServiceInstance(true)
        then:
        response.statusCode == ErrorCode.SERVICE_UPDATE_NOT_ALLOWED.httpStatus
    }

}
