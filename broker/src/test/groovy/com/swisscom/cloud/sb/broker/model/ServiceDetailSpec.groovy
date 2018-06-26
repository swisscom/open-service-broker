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
