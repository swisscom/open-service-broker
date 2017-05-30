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
