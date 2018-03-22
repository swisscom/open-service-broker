package com.swisscom.cloud.sb.broker.binding

import com.fasterxml.jackson.annotation.JsonProperty

class ServiceInstanceBindingResponseDto {
    @JsonProperty("credentials")
    String credentials
    @JsonProperty("syslog_drain_url")
    String syslogDrainUrl
    @JsonProperty("route_service_url")
    String routeServiceUrl
    @JsonProperty("volume_mounts")
    Object[] volumeMounts
    @JsonProperty("parameters")
    def parameters = [:]
}
