package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.swisscom.cloud.sb.broker.servicedefinition.dto.ParameterDto
import groovy.transform.CompileStatic
import org.springframework.cloud.servicebroker.model.Schemas

@CompileStatic
class PlanDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String id
    String name
    String description
    boolean free
    Map<String, Object> metadata
    List<ParameterDto> containerParams
    Schemas schemas
}
