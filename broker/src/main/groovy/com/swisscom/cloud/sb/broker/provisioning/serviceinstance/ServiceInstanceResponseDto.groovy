package com.swisscom.cloud.sb.broker.provisioning.serviceinstance

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import groovy.transform.CompileStatic

@CompileStatic
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceInstanceResponseDto {
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
    @JsonSerialize
    @JsonProperty("details")
    def details = [:]
}