package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.converter.AbstractGenericConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.CatalogDto
import com.swisscom.cloud.sb.broker.model.CFService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@CompileStatic
@Component
class CatalogDtoConverter extends AbstractGenericConverter<Collection<CFService>, CatalogDto> {
    @Autowired
    protected CFServiceDtoConverter cfServiceDtoConverter

    @Override
    void convert(Collection<CFService> source, CatalogDto prototype) {
        prototype.services = cfServiceDtoConverter.convertAll(source.sort { it.displayIndex })
    }
}