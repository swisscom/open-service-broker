package com.swisscom.cf.broker.cfextensions.endpoint

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class EndpointDto implements Serializable {
    @JsonSerialize
    @JsonProperty("destination")
    String destination
    @JsonSerialize
    @JsonProperty("ports")
    String ports
    @JsonSerialize
    @JsonProperty("protocol")
    String protocol
}
