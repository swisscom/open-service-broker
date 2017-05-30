package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.PlanMetadata
import com.swisscom.cloud.sb.broker.cfapi.dto.PlanDto
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
