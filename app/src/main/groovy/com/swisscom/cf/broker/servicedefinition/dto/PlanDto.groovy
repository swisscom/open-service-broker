package com.swisscom.cf.broker.servicedefinition.dto

import groovy.transform.CompileStatic

@CompileStatic
class PlanDto extends com.swisscom.cf.broker.cfapi.dto.PlanDto {
    String guid
    String templateId
    String internalName
    int displayIndex
    boolean asyncRequired
    int maxBackups
    List<ParameterDto> parameters
}

