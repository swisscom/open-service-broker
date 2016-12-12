package com.swisscom.cf.broker.cfapi.converter

import com.swisscom.cf.broker.cfapi.dto.CatalogDto
import com.swisscom.cf.broker.converter.AbstractGenericConverter
import com.swisscom.cf.broker.model.CFService
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