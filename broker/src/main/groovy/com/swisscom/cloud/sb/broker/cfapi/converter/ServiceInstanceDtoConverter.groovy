package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.ServiceInstance
import com.swisscom.cloud.sb.broker.provisioning.serviceinstance.ServiceInstanceResponseDto
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ServiceInstanceDtoConverter extends AbstractGenericConverter<ServiceInstance, ServiceInstanceResponseDto> {

    @Override
    void convert(ServiceInstance source, ServiceInstanceResponseDto prototype) {
        prototype.serviceId = source.plan.service.guid
        prototype.planId = source.plan.guid
        prototype.dashboardUrl = null
        prototype.parameters = source.parameters
    }
}
