package com.swisscom.cloud.sb.broker.servicedefinition.converter

import com.swisscom.cloud.sb.broker.cfapi.converter.CFServiceDtoConverter
import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ServiceDto
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@CompileStatic
@Component
class ServiceDtoConverter extends AbstractGenericConverter<CFService, ServiceDto> {
    @Autowired
    CFServiceDtoConverter cfServiceDtoConverter

    @Autowired
    @Qualifier("ServiceDefinitionPlanDtoConverter")
    PlanDtoConverter planDtoConverter

    @Override
    protected void convert(CFService source, ServiceDto prototype) {
        cfServiceDtoConverter.convert(source, prototype)
        prototype.id = null
        prototype.guid = source.guid
        prototype.internalName = source.internalName
        prototype.displayIndex = source.displayIndex
        prototype.asyncRequired = source.asyncRequired
        prototype.plans = planDtoConverter.convertAll(source.plans)
    }
}
