package com.swisscom.cf.broker.controller

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.swisscom.cf.broker.cfapi.converter.CatalogDtoConverter
import com.swisscom.cf.broker.cfapi.dto.CatalogDto
import com.swisscom.cf.broker.model.repository.CFServiceRepository
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@CompileStatic
class CatalogController extends BaseController {

    @VisibleForTesting
    @Autowired
    private CatalogDtoConverter catalogDtoConverter

    @Autowired
    private CFServiceRepository serviceRepository

    @RequestMapping(value = "/v2/catalog", method = RequestMethod.GET)
    public CatalogDto getCatalog() {
        return catalogDtoConverter.convert(Lists.newArrayList(serviceRepository.findAll()))
    }

}
