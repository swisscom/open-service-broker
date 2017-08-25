package com.swisscom.cloud.sb.broker.controller

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.swisscom.cloud.sb.broker.cfapi.converter.CatalogDtoConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.CatalogDto
import com.swisscom.cloud.sb.broker.model.repository.CFServiceRepository
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Api(value = "Catalog", description = "Endpoint for catalog")
@RestController
@CompileStatic
class CatalogController extends BaseController {

    @VisibleForTesting
    @Autowired
    private CatalogDtoConverter catalogDtoConverter

    @Autowired
    private CFServiceRepository serviceRepository


    @ApiOperation(value = "List Catalog", response = CatalogDto.class,
            notes = "List the catalog for this broker")
    @RequestMapping(value = "/v2/catalog", method = RequestMethod.GET)
    public CatalogDto getCatalog() {
        return catalogDtoConverter.convert(Lists.newArrayList(serviceRepository.findAll()))
    }

}
