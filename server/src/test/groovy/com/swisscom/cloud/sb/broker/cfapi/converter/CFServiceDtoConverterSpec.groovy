package com.swisscom.cloud.sb.broker.cfapi.converter

import com.swisscom.cf.broker.model.*
import com.swisscom.cloud.sb.broker.cfapi.dto.CFServiceDto
import com.swisscom.cloud.sb.broker.model.CFService
import com.swisscom.cloud.sb.broker.model.CFServiceMetadata
import com.swisscom.cloud.sb.broker.model.CFServicePermission
import com.swisscom.cloud.sb.broker.model.Plan
import com.swisscom.cloud.sb.broker.model.Tag
import spock.lang.Specification

class CFServiceDtoConverterSpec extends Specification {
    private CFServiceDtoConverter serviceDtoConverter

    def setup() {
        serviceDtoConverter = new CFServiceDtoConverter()
        serviceDtoConverter.planDtoConverter = Mock(PlanDtoConverter)
        serviceDtoConverter.dashboardClientDtoConverter = Mock(DashboardClientDtoConverter)
    }

    def "conversion works correctly"() {
        given:
        def service = new CFService()
        service.name = "name"
        service.guid = "guid"
        service.description = "description"
        service.bindable = true
        and:
        service.plans = [new Plan()]
        and:
        service.tags = [new Tag(tag: "tag1")]
        and:
        service.permissions = [new CFServicePermission(permission: CFServicePermission.SYSLOG_DRAIN)]
        and:
        service.metadata.add(new CFServiceMetadata(key: 'someKey', value: "displayName"))

        when:
        CFServiceDto cfServiceDto = this.serviceDtoConverter.convert(service)
        then:
        cfServiceDto.name == "name"
        cfServiceDto.id == "guid"
        cfServiceDto.description == "description"
        cfServiceDto.bindable
        cfServiceDto.tags.size() == 1 && cfServiceDto.tags.get(0) == "tag1"
        cfServiceDto.requires.size() == 1 && cfServiceDto.requires.get(0) == CFServicePermission.SYSLOG_DRAIN
        cfServiceDto.metadata.get('someKey') == "displayName"

        and:
        1 * serviceDtoConverter.planDtoConverter.convertAll { it.size() == 1 }
        1 * serviceDtoConverter.dashboardClientDtoConverter.convert(service)
    }

    def "plans are sorted correctly"() {
        given:
        CFService service = new CFService(plans:
                [new Plan(displayIndex: 1),
                 new Plan(displayIndex: 0),
                 new Plan(displayIndex: 2)])
        when:
        serviceDtoConverter.convert(service)
        then:
        1 * serviceDtoConverter.dashboardClientDtoConverter.convert(service)
        1 * serviceDtoConverter.planDtoConverter.convertAll {
            Collection<Plan> c -> c.eachWithIndex { Plan entry, int i -> assert i == entry.displayIndex }
        }
    }
}
