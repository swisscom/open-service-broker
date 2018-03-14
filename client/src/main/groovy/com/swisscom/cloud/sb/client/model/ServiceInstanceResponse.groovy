package com.swisscom.cloud.sb.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import groovy.transform.CompileStatic

@CompileStatic
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceInstanceResponse {
    @JsonSerialize
    @JsonProperty("service_id")
    String serviceId
    @JsonSerialize
    @JsonProperty("plan_id")
    String planId
    @JsonSerialize
    @JsonProperty("dashboard_url")
    String dashboardUrl
    @JsonSerialize
    @JsonProperty("parameters")
    def parameters = [:]
}