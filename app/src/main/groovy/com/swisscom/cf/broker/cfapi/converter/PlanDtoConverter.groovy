package com.swisscom.cf.broker.cfapi.converter

import com.swisscom.cf.broker.cfapi.dto.PlanDto
import com.swisscom.cf.broker.converter.AbstractGenericConverter
import com.swisscom.cf.broker.model.Plan
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import static com.swisscom.cf.broker.cfapi.converter.MetadataJsonHelper.getValue

@Component
@CompileStatic
class PlanDtoConverter extends AbstractGenericConverter<Plan, PlanDto> {

    @Override
    void convert(Plan source, PlanDto prototype) {
        prototype.id = source.guid
        prototype.name = source.name
        prototype.description = source.description
        prototype.free = source.free
        prototype.metadata = convertMetadata(source)
    }

    private Map<String, Object> convertMetadata(Plan plan) {
        Map<String, Object> result = [:]
        plan.metadata.each { result[it.key] = getValue(it.type, it.value) }
        result
    }
}
