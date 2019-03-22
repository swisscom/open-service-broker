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

import com.swisscom.cloud.sb.broker.cfapi.dto.PlanDto
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.PlanMetadata
import spock.lang.Specification
import spock.lang.Unroll

class PlanDtoConverterSpec extends Specification {
    def "conversion works correctly"() {
        given:
        def planDtoConverter = new PlanDtoConverter()
        def plan = new Plan()
        plan.guid = "guid"
        plan.name = "name"
        plan.description = "description"
        plan.free = true
        and:
        when:
        def dto = planDtoConverter.convert(plan)
        then:
        dto.id == "guid"
        dto.name == "name"
        dto.description == "description"
        plan.free
        0 * _._
    }

    @Unroll("Value:#value with type:# should yield #result")
    def "conversion with type works correctly"() {
        given:
        def planDtoConverter = new PlanDtoConverter()
        def plan = new Plan(metadata: [new PlanMetadata(key: "key1", value: value, type: type)])
        when:
        PlanDto planMetadataDto = planDtoConverter.convert(plan)
        then:
        planMetadataDto.metadata.get('key1') == result
        where:
        value                   | type                      | result
        "value"                 | null                      | "value"
        "True"                  | 'Boolean'                 | true
        "True"                  | 'bool'                    | true
        "1"                     | 'int'                     | 1
        "1"                     | 'Integer'                 | 1
        "[\"woop\",\"mauz\"]"   | ArrayList.class.name      | [ "woop", "mauz" ]
        "\"1\""                 | String.class.name         | "1"
    }
}
