package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.CompileStatic

@CompileStatic
class PlanDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String id
    String name
    String description
    boolean free
    Map<String, Object> metadata
}
