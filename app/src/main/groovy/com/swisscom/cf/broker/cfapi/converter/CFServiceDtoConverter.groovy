package com.swisscom.cf.broker.cfapi.converter

import com.swisscom.cf.broker.cfapi.dto.CFServiceDto
import com.swisscom.cf.broker.converter.AbstractGenericConverter
import com.swisscom.cf.broker.model.CFService
import com.swisscom.cf.broker.model.CFServicePermission
import com.swisscom.cf.broker.model.Tag
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class CFServiceDtoConverter extends AbstractGenericConverter<CFService, CFServiceDto> {
    @Autowired
    protected PlanDtoConverter planDtoConverter

    @Autowired
    protected DashboardClientDtoConverter dashboardClientDtoConverter

    @Override
    void convert(CFService source, CFServiceDto prototype) {
        prototype.id = source.guid
        prototype.name = source.name
        prototype.description = source.description
        prototype.bindable = source.bindable
        prototype.metadata = convertMetadata(source)
        prototype.plans = planDtoConverter.convertAll(source.plans.sort { it.displayIndex })
        prototype.tags = source.tags.collect { Tag t -> t.tag }
        prototype.requires = source.permissions.collect { CFServicePermission p -> p.permission }
        prototype.dashboard_client = dashboardClientDtoConverter.convert(source)
    }

    private Map<String, Object> convertMetadata(CFService service) {
        Map<String, Object> result = [:]
        service.metadata.each { result[it.key] = MetadataJsonHelper.getValue(it.type, it.value) }
        result
    }
}
