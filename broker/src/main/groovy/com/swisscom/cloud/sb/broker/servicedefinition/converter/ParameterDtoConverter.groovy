package com.swisscom.cloud.sb.broker.servicedefinition.converter

import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Parameter
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ParameterDto
import org.springframework.stereotype.Component

@Component
class ParameterDtoConverter extends AbstractGenericConverter<Parameter, ParameterDto> {
    @Override
    protected void convert(Parameter source, ParameterDto prototype) {
        prototype.name = source.name
        prototype.value = source.value
        prototype.template = source.template
    }
}
