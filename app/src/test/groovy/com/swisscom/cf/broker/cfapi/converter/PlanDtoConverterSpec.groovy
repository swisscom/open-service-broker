package com.swisscom.cf.broker.cfapi.converter

import com.swisscom.cf.broker.cfapi.dto.PlanDto
import com.swisscom.cf.broker.model.Plan
import com.swisscom.cf.broker.model.PlanMetadata
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
        value   | type      | result
        "value" | null      | "value"
        "True"  | 'Boolean' | true
        "True"  | 'bool'    | true
        "1"     | 'int'     | 1
        "1"     | 'Integer' | 1
    }

}
