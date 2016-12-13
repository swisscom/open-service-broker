package com.swisscom.cf.broker.cfapi.converter

import com.swisscom.cf.broker.cfapi.dto.ErrorDto
import com.swisscom.cf.broker.converter.AbstractGenericConverter
import com.swisscom.cf.broker.exception.ServiceBrokerException
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ErrorDtoConverter extends AbstractGenericConverter<ServiceBrokerException, ErrorDto> {
    @Override
    protected void convert(ServiceBrokerException source, ErrorDto prototype) {
        prototype.code = source.code
        prototype.description = source.description
        prototype.error_code = source.error_code
    }
}
