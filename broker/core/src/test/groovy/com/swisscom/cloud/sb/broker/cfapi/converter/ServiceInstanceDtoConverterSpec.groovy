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

package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import spock.lang.Specification


class ServiceInstanceDtoConverterSpec extends Specification {

    def 'happy path: convert'() {
        given:
        def testee = new ServiceInstanceDtoConverter()
        def serviceId = UUID.randomUUID().toString()
        def planId = UUID.randomUUID().toString()

        def serviceInstance = new ServiceInstance(
                plan: new Plan(guid: planId, service: new CFService(guid: serviceId)),
                parameters: "{}"
        )

        when:
        def dto = testee.convert(serviceInstance)

        then:
        dto.serviceId == serviceId
        dto.planId == planId
        dto.parameters == "{}"
    }

    def 'deleted children are not returned'() {
        given:
        def testee = new ServiceInstanceDtoConverter()
        def serviceId = UUID.randomUUID().toString()
        def planId = UUID.randomUUID().toString()
        def exist1 = UUID.randomUUID().toString()
        def exist2 = UUID.randomUUID().toString()
        def deleted1 = UUID.randomUUID().toString()

        def serviceInstance = new ServiceInstance(
                plan: new Plan(guid: planId, service: new CFService(guid: serviceId)),
                parameters: "{}",
                childs: [
                        new ServiceInstance(guid: exist1, deleted: false),
                        new ServiceInstance(guid: deleted1, deleted: true),
                        new ServiceInstance(guid: exist2, deleted: false)
                ]
        )

        when:
        def dto = testee.convert(serviceInstance)

        then:
        dto.serviceId == serviceId
        dto.planId == planId
        dto.parameters == "{}"
        dto.childInstances.size() == 2
        dto.childInstances.any { it -> it == exist1 }
        dto.childInstances.any { it -> it == exist2 }
        dto.childInstances.any { it -> it == deleted1 } == false

    }

}