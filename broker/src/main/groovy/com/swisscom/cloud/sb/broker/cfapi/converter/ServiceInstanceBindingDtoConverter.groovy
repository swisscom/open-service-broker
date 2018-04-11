package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.binding.ServiceInstanceBindingResponseDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
class ServiceInstanceBindingDtoConverter extends AbstractGenericConverter<ServiceBinding, ServiceInstanceBindingResponseDto> {

    @Override
    void convert(ServiceBinding source, ServiceInstanceBindingResponseDto prototype) {
        Object credentials = new JsonSlurper().parseText(source.credentials).credentials
        prototype.credentials = new JsonBuilder(credentials).toString()
        prototype.parameters = source.parameters
        prototype.routeServiceUrl = null
        prototype.syslogDrainUrl = null
        prototype.volumeMounts = null
    }
}