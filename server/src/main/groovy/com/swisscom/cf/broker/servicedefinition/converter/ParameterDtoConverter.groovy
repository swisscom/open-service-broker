package com.swisscom.cf.broker.servicedefinition.converter

import com.swisscom.cf.broker.converter.AbstractGenericConverter
import com.swisscom.cf.broker.model.Parameter
import com.swisscom.cf.broker.servicedefinition.dto.ParameterDto
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
