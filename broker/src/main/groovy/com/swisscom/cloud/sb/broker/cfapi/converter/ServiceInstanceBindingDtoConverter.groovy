package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.binding.CredentialService
import com.swisscom.cloud.sb.broker.binding.ServiceInstanceBindingResponseDto
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceBinding
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class ServiceInstanceBindingDtoConverter extends AbstractGenericConverter<ServiceBinding, ServiceInstanceBindingResponseDto> {

    @Autowired
    private CredentialService credentialService

    @Override
    void convert(ServiceBinding source, ServiceInstanceBindingResponseDto prototype) {
        prototype.credentials = credentialService.getCredential(source)
        prototype.parameters = source.parameters
        prototype.details = source.details
        prototype.routeServiceUrl = null
        prototype.syslogDrainUrl = null
        prototype.volumeMounts = null
    }

}