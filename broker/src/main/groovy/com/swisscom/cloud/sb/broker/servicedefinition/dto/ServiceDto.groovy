package com.swisscom.cloud.sb.broker.servicedefinition.dto

import com.swisscom.cloud.sb.broker.cfapi.dto.CFServiceDto
import groovy.transform.CompileStatic

@CompileStatic
class ServiceDto extends CFServiceDto {
    String guid
    String internalName
    String serviceProviderClassName
    int displayIndex
    boolean asyncRequired
    List<PlanDto> plans
}
