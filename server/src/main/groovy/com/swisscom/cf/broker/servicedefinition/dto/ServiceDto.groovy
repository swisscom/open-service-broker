package com.swisscom.cf.broker.servicedefinition.dto

import com.swisscom.cf.broker.cfapi.dto.CFServiceDto
import groovy.transform.CompileStatic

@CompileStatic
class ServiceDto extends CFServiceDto {
    String guid
    String internalName
    int displayIndex
    boolean asyncRequired
    List<PlanDto> plans
}
