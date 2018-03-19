package com.swisscom.cloud.sb.client.model

import com.fasterxml.jackson.annotation.JsonProperty

class ServiceInstanceBindingResponse {
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
