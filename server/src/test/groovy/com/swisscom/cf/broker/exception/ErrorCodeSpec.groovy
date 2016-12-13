package com.swisscom.cf.broker.exception

import spock.lang.Specification

class ErrorCodeSpec extends Specification {
    def "ToMdString"() {
        expect:
        print(ErrorCode.toMdString())
    }

    def "Error description is created correctly when *no* additional description is provided"() {
        when:
        ErrorCode.ATMOS_AUTHENTICATION_FAILED.throwNew()
        then:
        def ex = thrown(ServiceBrokerException)
        ex.description == ErrorCode.ATMOS_AUTHENTICATION_FAILED.description
    }

    def "Error description is created correctly when  additional description is provided"() {
        when:
        ErrorCode.ATMOS_AUTHENTICATION_FAILED.throwNew('bla')
        then:
        def ex = thrown(ServiceBrokerException)
        ex.description == ErrorCode.ATMOS_AUTHENTICATION_FAILED.description + ' bla'
    }

}
