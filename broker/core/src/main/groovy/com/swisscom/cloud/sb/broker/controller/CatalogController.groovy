/*
 * Copyright (c) 2018 Swisscom (Switzerland) Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.swisscom.cloud.sb.broker.controller

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.swisscom.cloud.sb.broker.cfapi.converter.CatalogDtoConverter
import com.swisscom.cloud.sb.broker.cfapi.dto.CatalogDto
import com.swisscom.cloud.sb.broker.repository.CFServiceRepository
import groovy.transform.CompileStatic
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@Api(value = "Catalog", description = "Endpoint for catalog")
@RestController
@CompileStatic
class CatalogController extends BaseController {

    @VisibleForTesting
    private CatalogDtoConverter catalogDtoConverter
    private CFServiceRepository serviceRepository

    CatalogController(CatalogDtoConverter catalogDtoConverter,
                      CFServiceRepository cfServiceRepository) {
        this.catalogDtoConverter = catalogDtoConverter
        this.serviceRepository = cfServiceRepository
    }


    @ApiOperation(value = "List Catalog", response = CatalogDto.class,
            notes = "List the catalog for this broker")
    @RequestMapping(value = "/v2/catalog", method = RequestMethod.GET)
    public CatalogDto getCatalog() {
        return catalogDtoConverter.convert(Lists.newArrayList(serviceRepository.findAll()))
    }

}
