package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.binding.ServiceInstanceBindingResponseDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ServiceInstanceBindingDtoConverter extends AbstractGenericConverter<ServiceBinding, ServiceInstanceBindingResponseDto> {

    @Override
    void convert(ServiceBinding source, ServiceInstanceBindingResponseDto prototype) {
        prototype.credentials = source.credentials
        prototype.parameters = null // TODO
        prototype.routeServiceUrl = null
        prototype.syslogDrainUrl = null
        prototype.volumeMounts = null
    }

}
