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

package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cloud.sb.broker.cfapi.dto.CFServiceDto
import com.swisscom.cloud.sb.broker.cfapi.dto.CatalogDto
import com.swisscom.cloud.sb.broker.model.CFService
import spock.lang.Specification

class CatalogDtoConverterSpec extends Specification {
    private CatalogDtoConverter catalogDtoConverter = new CatalogDtoConverter()

    def setup() {
        catalogDtoConverter.cfServiceDtoConverter = Mock(CFServiceDtoConverter)
    }

    def "conversion works correctly"() {
        given:
        def serviceList = [new CFService()]
        def serviceDtoList = [new CFServiceDto()]
        1 * catalogDtoConverter.cfServiceDtoConverter.convertAll(serviceList) >> serviceDtoList
        when:
        CatalogDto dto = this.catalogDtoConverter.convert(serviceList)
        then:
        dto.services == serviceDtoList
    }

    def "services are sorted correctly"() {
        given:
        List<CFService> serviceList = [new CFService(displayIndex: 1),
                                       new CFService(displayIndex: 0),
                                       new CFService(displayIndex: 2)]
        when:
        catalogDtoConverter.convert(serviceList)
        then:
        1 * catalogDtoConverter.cfServiceDtoConverter.convertAll {
            Collection<CFService> c -> c.eachWithIndex { CFService entry, int i -> assert i == entry.displayIndex }
        }
    }
}
