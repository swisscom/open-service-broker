package com.swisscom.cloud.sb.broker.model

import spock.lang.Specification

class ServiceDetailSpec extends Specification {

    void "two different ServiceDetails with the same ID are equal"() {
        given:
        def serviceDetailOne = new ServiceDetail(id: 100, key: "some Key", value: "some Value", uniqueKey: false)
        def serviceDetailTwo = new ServiceDetail(id: 100, key: "some other Key", value: "some other Value", uniqueKey: false)

        expect:
        serviceDetailOne == serviceDetailTwo
    }

    void "two different ServiceDetails with different ID are not equal"() {
        given:
        def serviceDetailOne = new ServiceDetail(id: 100, key: "some Key", value: "some Value", uniqueKey: false)
        def serviceDetailTwo = new ServiceDetail(id: 101, key: "some other Key", value: "some other Value", uniqueKey: false)

        expect:
        serviceDetailOne != serviceDetailTwo
    }

    void "two different ServiceDetails without ID are not equal"() {
        given:
        def serviceDetailOne = new ServiceDetail(key: "some Key", value: "some Value", uniqueKey: false)
        def serviceDetailTwo = new ServiceDetail(key: "some other Key", value: "some other Value", uniqueKey: false)

        expect:
        serviceDetailOne != serviceDetailTwo
    }

    void "two ServiceDetails with same key and unique key are equal"() {
        given:
        def serviceDetailOne = new ServiceDetail(key: "some Key", value: "some Value", uniqueKey: true)
        def serviceDetailTwo = new ServiceDetail(key: "some Key", value: "some other Value", uniqueKey: true)

        expect:
        serviceDetailOne == serviceDetailTwo
    }

    void "two ServiceDetails with same key and different ids and unique key are equal"() {
        given:
        def serviceDetailOne = new ServiceDetail(id: 200, key: "some Key", value: "some Value", uniqueKey: true)
        def serviceDetailTwo = new ServiceDetail(id: 201, key: "some Key", value: "some other Value", uniqueKey: true)

        expect:
        serviceDetailOne == serviceDetailTwo
    }
}
