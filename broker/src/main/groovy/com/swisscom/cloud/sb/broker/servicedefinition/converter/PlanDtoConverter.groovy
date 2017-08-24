package com.swisscom.cloud.sb.broker.servicedefinition.converter

import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.servicedefinition.dto.PlanDto
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@CompileStatic
@Component("ServiceDefinitionPlanDtoConverter")
class PlanDtoConverter extends AbstractGenericConverter<Plan, PlanDto> {
    @Autowired
    com.swisscom.cloud.sb.broker.cfapi.converter.PlanDtoConverter planDtoConverter
    @Autowired
    ParameterDtoConverter containerParamterDtoConverter

    @Override
    protected void convert(Plan source, PlanDto prototype) {
        planDtoConverter.convert(source, prototype)
        prototype.id = null
        prototype.guid = source.guid
        prototype.internalName = source.internalName
        prototype.displayIndex = source.displayIndex
        prototype.asyncRequired = source.asyncRequired
        prototype.templateId = source.templateUniqueIdentifier
        prototype.templateVersion = source.templateVersion
        prototype.maxBackups = source.maxBackups ?: 0
        prototype.parameters = containerParamterDtoConverter.convertAll(source.parameters)
    }
}
