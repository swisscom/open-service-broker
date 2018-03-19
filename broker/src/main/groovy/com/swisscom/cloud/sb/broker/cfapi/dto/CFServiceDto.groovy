package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.CompileStatic

@CompileStatic
class CFServiceDto implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String id
    String name
    String description
    boolean bindable
    List<PlanDto> plans
    List<String> tags
    List<String> requires
    Map<String, Object> metadata = new HashMap<String, Object>()
    DashboardClientDto dashboard_client
    boolean plan_updateable
    Boolean instancesRetrievable
    Boolean bindingsRetrievable
}
