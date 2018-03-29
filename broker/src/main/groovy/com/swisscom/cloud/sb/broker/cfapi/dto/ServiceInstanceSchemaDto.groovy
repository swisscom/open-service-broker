package com.swisscom.cloud.sb.broker.cfapi.dto

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
class ServiceInstanceSchemaDto {

    @JsonSerialize
    @JsonProperty("create")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MethodSchemaDto createMethodSchema = null

    @JsonSerialize
    @JsonProperty("update")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MethodSchemaDto updateMethodSchema = null

    ServiceInstanceSchemaDto(MethodSchemaDto createMethodSchema,
                             MethodSchemaDto updateMethodSchema) {
        this.createMethodSchema = createMethodSchema
        this.updateMethodSchema = updateMethodSchema
    }
}
