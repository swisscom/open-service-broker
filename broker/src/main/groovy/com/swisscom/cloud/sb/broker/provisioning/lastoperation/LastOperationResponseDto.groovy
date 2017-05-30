package com.swisscom.cloud.sb.broker.provisioning.lastoperation

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import groovy.transform.CompileStatic

@CompileStatic
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class LastOperationResponseDto {
    @JsonSerialize
    @JsonProperty("state")
    CFLastOperationStatus status
    @JsonSerialize
    @JsonProperty("description")
    String description
}